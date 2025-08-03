import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.2" apply true
    id("io.spring.dependency-management") version "1.1.6" apply true
    kotlin("jvm") version "1.9.24" apply true
    kotlin("plugin.spring") version "1.9.24" apply true
}


group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val analysis by configurations.creating


subprojects {
    repositories {
        mavenCentral()
    }
    
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation(kotlin("test"))
    }
}


dependencies {
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 메인 클래스 설정
springBoot {
    mainClass.set("com.example.inker.application.InkerApplicationKt")
}

// API 문서 생성 태스크
tasks.register("apiDocuments") {
    group = "documentation"
    description = "Controller 분석하여 HTML 문서 생성"
    
    doLast {
        ControllerAnalyzer().analyze()
    }
}

// 분석 설정
val analysisConfig = mapOf(
    "outputFile" to "build/controller-docs.html",
    "includeKotlin" to true,
    "includeJava" to true,
    "excludeDirs" to listOf("build", "gradle", ".git"),
    "controllerAnnotations" to listOf("@RestController", "@Controller")
)

// Controller 분석기 클래스
class ControllerAnalyzer {
    private val analyzedExceptions = mutableSetOf<String>()
    
    fun analyze() {
        println("🔍 Controller 분석을 시작합니다...")
        
        val controllers = mutableListOf<ControllerInfo>()
        
        // 전체 프로젝트에서 Controller 찾기
        findControllersInProject(controllers)
        
        // JSON 생성
        val json = generateJson(controllers)
        
        // HTML 생성
        val outputFile = analysisConfig["outputFile"] as String
        generateHtml(json, controllers, outputFile)
        
        println("✅ Controller 분석이 완료되었습니다!")
        println("📄 HTML 파일이 생성되었습니다: $outputFile")
        println("📊 분석 결과: ${controllers.size}개 Controller, ${controllers.sumOf { it.endpoints.size }}개 Endpoint")
    }
    
    private fun findControllersInProject(controllers: MutableList<ControllerInfo>) {
        // 프로젝트 루트 디렉토리에서 모든 서브프로젝트 찾기
        val projectRoot = file(".")
        val excludeDirs = analysisConfig["excludeDirs"] as List<String>
        val subprojects = projectRoot.listFiles()?.filter { 
            it.isDirectory && 
            !excludeDirs.contains(it.name) &&
            file("${it.name}/build.gradle.kts").exists()
        } ?: emptyList()
        
        println("📦 발견된 서브프로젝트: ${subprojects.map { it.name }}")
        
        subprojects.forEach { subproject ->
            val moduleName = subproject.name
            val kotlinSourcePath = "${moduleName}/src/main/kotlin"
            val javaSourcePath = "${moduleName}/src/main/java"
            
            // Kotlin 소스 디렉토리 분석
            if (analysisConfig["includeKotlin"] as Boolean && file(kotlinSourcePath).exists()) {
                println("🔍 $moduleName 모듈의 Kotlin 소스 분석 중...")
                analyzeModuleControllers(moduleName, kotlinSourcePath, controllers)
            }
            
            // Java 소스 디렉토리 분석
            if (analysisConfig["includeJava"] as Boolean && file(javaSourcePath).exists()) {
                println("🔍 $moduleName 모듈의 Java 소스 분석 중...")
                analyzeModuleControllers(moduleName, javaSourcePath, controllers)
            }
        }
    }
    
    private fun analyzeModuleControllers(module: String, modulePath: String, controllers: MutableList<ControllerInfo>) {
        val controllerDir = file(modulePath)
        if (!controllerDir.exists()) return
        
        controllerDir.walkTopDown()
            .filter { it.extension in listOf("kt", "java") }
            .forEach { file ->
                val content = file.readText()
                val controllerAnnotations = analysisConfig["controllerAnnotations"] as List<String>
                if (controllerAnnotations.any { content.contains(it) }) {
                    println("📄 Controller 발견: ${file.name}")
                    val controllerInfo = parseController(file, content, module)
                    controllers.add(controllerInfo)
                }
            }
    }
    
    private fun parseController(file: File, content: String, module: String): ControllerInfo {
        val lines = content.lines()
        var className = ""
        var basePath = ""
        val endpoints = mutableListOf<EndpointInfo>()
        
        var currentClass = ""
        var currentBasePath = ""
        var inClass = false
        
        // Service 예외를 한 번만 분석
        var serviceExceptions = emptyList<ExceptionInfo>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // @RequestMapping 찾기 (클래스 선언 전에도 처리)
            if (trimmedLine.startsWith("@RequestMapping(")) {
                currentBasePath = extractPathFromAnnotation(trimmedLine)
                basePath = currentBasePath
                println("🔍 Base path 발견: $basePath")
            }
            
            // 클래스 이름 찾기
            if (trimmedLine.startsWith("class ") && trimmedLine.contains("Controller")) {
                currentClass = trimmedLine.substringAfter("class ").substringBefore("(").substringBefore(":")
                className = currentClass
                inClass = true
                println("🔍 Controller 클래스 발견: $className")
                
                // Service 예외 분석 (한 번만)
                serviceExceptions = analyzeServiceExceptions(className, module)
            }
            
            // Java 클래스도 찾기
            if (trimmedLine.contains("public class ") && trimmedLine.contains("Controller")) {
                currentClass = trimmedLine.substringAfter("public class ").substringBefore("(").substringBefore(":").substringBefore(" {").trim()
                className = currentClass
                inClass = true
                println("🔍 Java Controller 클래스 발견: $className")
                
                // Service 예외 분석 (한 번만)
                serviceExceptions = analyzeServiceExceptions(className, module)
            }
            
            // 디버깅: 모든 @RequestMapping 라인 확인
            if (trimmedLine.contains("@RequestMapping")) {
                println("🔍 @RequestMapping 라인 발견: $trimmedLine")
                println("🔍 inClass: $inClass")
            }
            
            // HTTP 메서드 어노테이션 찾기
            if (trimmedLine.startsWith("@GetMapping") || 
                trimmedLine.startsWith("@PostMapping") ||
                trimmedLine.startsWith("@PutMapping") ||
                trimmedLine.startsWith("@DeleteMapping") ||
                trimmedLine.startsWith("@PatchMapping")) {
                
                val endpoint = parseEndpoint(trimmedLine, lines, currentBasePath, lines.indexOf(line), module, className, serviceExceptions)
                if (endpoint != null) {
                    endpoints.add(endpoint)
                }
            }
        }
        
        return ControllerInfo(
            module = module,
            fileName = file.name,
            className = className,
            basePath = basePath,
            endpoints = endpoints
        )
    }
    
    private fun extractPathFromAnnotation(annotation: String): String {
        val pathMatch = Regex("\"([^\"]*)\"").find(annotation)
        return pathMatch?.groupValues?.get(1) ?: ""
    }
    
    private fun parseEndpoint(annotationLine: String, lines: List<String>, basePath: String, annotationIndex: Int, module: String, controllerName: String, serviceExceptions: List<ExceptionInfo>): EndpointInfo? {
        val method = when {
            annotationLine.startsWith("@GetMapping") -> "GET"
            annotationLine.startsWith("@PostMapping") -> "POST"
            annotationLine.startsWith("@PutMapping") -> "PUT"
            annotationLine.startsWith("@DeleteMapping") -> "DELETE"
            annotationLine.startsWith("@PatchMapping") -> "PATCH"
            else -> "GET"
        }
        
        val path = extractPathFromAnnotation(annotationLine)
        val fullPath = if (path.startsWith("/")) "$basePath$path" else "$basePath/$path"
        
        // 함수 시그니처 찾기
        val functionSignature = findFunctionSignature(lines, annotationIndex)
        
        // 상세 파라미터 분석
        val requestDetails = analyzeRequestDetails(lines, annotationIndex)
        val responseDetails = analyzeResponseDetails(functionSignature.second)
        
        // 함수 본문에서 예외 분석
        val functionBody = extractFunctionBody(lines, annotationIndex)
        val controllerExceptions = analyzeExceptionsInFunction(functionBody, annotationIndex)
        
        // 모든 예외 합치고 중복 제거
        val allExceptions = controllerExceptions + serviceExceptions
        val exceptions = allExceptions.distinctBy { it.exceptionType }
        
        return EndpointInfo(
            method = method,
            path = fullPath,
            requestType = functionSignature.first,
            responseType = functionSignature.second,
            requestDetails = requestDetails,
            responseDetails = responseDetails,
            exceptions = exceptions
        )
    }
    
    private fun findFunctionSignature(lines: List<String>, startIndex: Int): Pair<String, String> {
        var functionLines = mutableListOf<String>()
        var foundFunction = false
        var parenCount = 0
        var braceCount = 0
        var angleCount = 0
        
        for (i in (startIndex + 1) until lines.size) {
            val line = lines[i]
            
            if (!foundFunction && (line.trim().startsWith("fun ") || line.trim().startsWith("public "))) {
                foundFunction = true
            }
            
            if (foundFunction) {
                functionLines.add(line)
                
                // 괄호 개수 세기
                parenCount += line.count { it == '(' } - line.count { it == ')' }
                braceCount += line.count { it == '{' } - line.count { it == '}' }
                angleCount += line.count { it == '<' } - line.count { it == '>' }
                
                // 함수 시그니처가 완료되면 (괄호가 닫히고 : 또는 { 가 나오면) 중단
                if (parenCount <= 0 && angleCount <= 0 && (line.trim().contains(":") || line.trim().contains("{"))) {
                    break
                }
                
                // 중괄호가 열리면 함수 본문 시작이므로 중단
                if (line.trim().startsWith("{")) {
                    break
                }
                
                // 중괄호가 열리면 함수 본문 시작이므로 중단
                if (braceCount > 0) {
                    break
                }
            }
        }
        
        if (foundFunction) {
            val fullSignature = functionLines.joinToString(" ")
            println("🔍 함수 시그니처: $fullSignature")
            
            // Request 타입 찾기
            val requestType = extractRequestType(fullSignature)
            
            // Response 타입 찾기
            val responseType = extractResponseType(fullSignature)
            
            return Pair(requestType, responseType)
        }
        
        return Pair("", "")
    }
    
    private fun extractFunctionBody(lines: List<String>, startIndex: Int): String {
        var braceCount = 0
        var inFunction = false
        val functionLines = mutableListOf<String>()
        
        for (i in startIndex until lines.size) {
            val line = lines[i]
            
            if (!inFunction && (line.trim().startsWith("fun ") || line.trim().startsWith("public "))) {
                inFunction = true
                functionLines.add(line)
                braceCount += line.count { it == '{' } - line.count { it == '}' }
            } else if (inFunction) {
                functionLines.add(line)
                braceCount += line.count { it == '{' } - line.count { it == '}' }
                
                if (braceCount <= 0) {
                    break
                }
            }
        }
        
        return functionLines.joinToString("\n")
    }
    
    private fun extractRequestType(functionLine: String): String {
        // @RequestBody request: CreateBookingRequest 형태 파싱 (Kotlin)
        if (functionLine.contains("@RequestBody")) {
            val requestBodyMatch = Regex("@RequestBody\\s+([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9.<>]+)").find(functionLine)
            if (requestBodyMatch != null) {
                val requestType = requestBodyMatch.groupValues[2] // 타입 부분 반환
                println("🔍 Request 타입 추출 (Kotlin): $requestType")
                return requestType
            }
            
            // @RequestBody CreateBookingRequest request 형태 파싱 (Kotlin)
            val simpleMatch = Regex("@RequestBody\\s+([A-Za-z0-9.<>]+)").find(functionLine)
            if (simpleMatch != null) {
                val requestType = simpleMatch.groupValues[1]
                println("🔍 Request 타입 추출 (Kotlin simple): $requestType")
                return requestType
            }
            
            // @RequestBody CreateStockRequest request 형태 파싱 (Java)
            val javaMatch = Regex("@RequestBody\\s+([A-Za-z0-9.<>]+)\\s+([a-zA-Z][a-zA-Z0-9]*)").find(functionLine)
            if (javaMatch != null) {
                val requestType = javaMatch.groupValues[1]
                println("🔍 Request 타입 추출 (Java): $requestType")
                return requestType
            }
        }
        
        // @PathVariable id: Long 형태 파싱 (Kotlin)
        if (functionLine.contains("@PathVariable")) {
            val pathVariableMatch = Regex("@PathVariable\\s+([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9.<>]+)").find(functionLine)
            if (pathVariableMatch != null) {
                val requestType = pathVariableMatch.groupValues[2] // 타입 부분 반환
                println("🔍 PathVariable 타입 추출 (Kotlin): $requestType")
                return requestType
            }
            
            // @PathVariable Long id 형태 파싱 (Kotlin)
            val simplePathVariableMatch = Regex("@PathVariable\\s+([A-Za-z0-9.<>]+)").find(functionLine)
            if (simplePathVariableMatch != null) {
                val requestType = simplePathVariableMatch.groupValues[1]
                println("🔍 PathVariable 타입 추출 (Kotlin simple): $requestType")
                return requestType
            }
            
            // @PathVariable Long id 형태 파싱 (Java)
            val javaPathVariableMatch = Regex("@PathVariable\\s+([A-Za-z0-9.<>]+)\\s+([a-zA-Z][a-zA-Z0-9]*)").find(functionLine)
            if (javaPathVariableMatch != null) {
                val requestType = javaPathVariableMatch.groupValues[1]
                println("🔍 PathVariable 타입 추출 (Java): $requestType")
                return requestType
            }
        }
        
        return ""
    }
    
    private fun extractResponseType(functionLine: String): String {
        // ResponseEntity<List<BookingResponse>> 형태 파싱
        if (functionLine.contains("ResponseEntity<")) {
            // 더 정확한 정규식으로 제네릭 타입 파싱
            val startIndex = functionLine.indexOf("ResponseEntity<") + "ResponseEntity<".length
            val endIndex = functionLine.lastIndexOf(">")
            if (startIndex > 0 && endIndex > startIndex) {
                val responseType = functionLine.substring(startIndex, endIndex)
                println("🔍 Response 타입 추출: $responseType")
                return responseType
            }
        }
        
        // Kotlin 함수 시그니처 파싱
        if (functionLine.contains(": ")) {
            val typeMatch = Regex(":\\s*([A-Za-z0-9.<>]+)").find(functionLine)
            if (typeMatch != null) {
                val responseType = typeMatch.groupValues[1]
                println("🔍 Kotlin Response 타입 추출: $responseType")
                return responseType
            }
        }
        
        // Java 함수 시그니처 파싱
        if (functionLine.contains("public ")) {
            val javaTypeMatch = Regex("public\\s+([A-Za-z0-9.<>]+)\\s+[a-zA-Z][a-zA-Z0-9]*\\s*\\(").find(functionLine)
            if (javaTypeMatch != null) {
                val responseType = javaTypeMatch.groupValues[1]
                println("🔍 Java Response 타입 추출: $responseType")
                return responseType
            }
        }
        
        return ""
    }
    
    private fun analyzeRequestDetails(lines: List<String>, startIndex: Int): RequestDetails {
        val parameters = mutableListOf<ParameterInfo>()
        val pathVariables = mutableListOf<String>()
        var bodyType: String? = null
        
        // 함수 시그니처 라인 찾기
        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("fun ") || line.startsWith("public ")) {
                // @PathVariable 파라미터 찾기 (Kotlin)
                val pathVariableMatches = Regex("@PathVariable\\s+([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9.<>]+)").findAll(line)
                pathVariableMatches.forEach { match ->
                    val paramName = match.groupValues[1]
                    val paramType = match.groupValues[2]
                    pathVariables.add(paramName)
                    parameters.add(ParameterInfo(paramName, paramType, "@PathVariable", true))
                }
                
                // @PathVariable 파라미터 찾기 (Java)
                val javaPathVariableMatches = Regex("@PathVariable\\s+([A-Za-z0-9.<>]+)\\s+([a-zA-Z][a-zA-Z0-9]*)").findAll(line)
                javaPathVariableMatches.forEach { match ->
                    val paramType = match.groupValues[1]
                    val paramName = match.groupValues[2]
                    pathVariables.add(paramName)
                    parameters.add(ParameterInfo(paramName, paramType, "@PathVariable", true))
                }
                
                // @RequestBody 파라미터 찾기 (Kotlin)
                val requestBodyMatch = Regex("@RequestBody\\s+([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9.<>]+)").find(line)
                if (requestBodyMatch != null) {
                    val paramName = requestBodyMatch.groupValues[1]
                    val paramType = requestBodyMatch.groupValues[2]
                    bodyType = paramType
                    parameters.add(ParameterInfo(paramName, paramType, "@RequestBody", true))
                }
                
                // @RequestBody 파라미터 찾기 (Java)
                val javaRequestBodyMatch = Regex("@RequestBody\\s+([A-Za-z0-9.<>]+)\\s+([a-zA-Z][a-zA-Z0-9]*)").find(line)
                if (javaRequestBodyMatch != null) {
                    val paramType = javaRequestBodyMatch.groupValues[1]
                    val paramName = javaRequestBodyMatch.groupValues[2]
                    bodyType = paramType
                    parameters.add(ParameterInfo(paramName, paramType, "@RequestBody", true))
                }
                
                break
            }
        }
        
        return RequestDetails(parameters, bodyType, pathVariables)
    }
    
    private fun analyzeResponseDetails(responseType: String): ResponseDetails {
        val isList = responseType.contains("List<") || responseType.contains("Array")
        val genericType = if (isList) {
            val match = Regex("List<([^>]+)>").find(responseType)
            match?.groupValues?.get(1)
        } else null
        val fields = when {
            isList && genericType != null -> {
                analyzeDtoFields(genericType)
            }
            responseType.isNotEmpty() && responseType != "Void" -> analyzeDtoFields(responseType)
            else -> emptyList()
        }
        return ResponseDetails(responseType, isList, genericType, fields)
    }
    
    private fun analyzeDtoFields(dtoType: String): List<FieldInfo> {
        val fields = mutableListOf<FieldInfo>()
        
        // DTO 파일들 찾기
        val dtoFiles = findDtoFiles(dtoType)
        println("🔍 DTO 타입 '$dtoType'에 대한 파일 ${dtoFiles.size}개 발견")
        
        dtoFiles.forEach { file ->
            println("📄 DTO 파일 분석: ${file.name}")
            val content = file.readText()
            val lines = content.lines()
            var inTargetClass = false
            var targetClassName = ""
            var inConstructor = false
            var constructorParams = ""
            
            for (line in lines) {
                val trimmedLine = line.trim()
                
                // Kotlin data class 찾기
                if (trimmedLine.startsWith("data class $dtoType(")) {
                    inTargetClass = true
                    targetClassName = dtoType
                    inConstructor = true
                    println("🔍 Kotlin data class 발견: $trimmedLine")
                    // 생성자 파라미터 추출
                    val paramStart = trimmedLine.indexOf("(")
                    val paramEnd = trimmedLine.indexOf(")")
                    if (paramStart != -1 && paramEnd != -1) {
                        constructorParams = trimmedLine.substring(paramStart + 1, paramEnd)
                        println("🔍 생성자 파라미터: $constructorParams")
                        val kotlinFields = parseKotlinParameters(constructorParams)
                        fields.addAll(kotlinFields)
                        println("🔍 파싱된 필드: ${kotlinFields.size}개")
                    }
                    continue
                }
                
                // Java class 찾기
                if (trimmedLine.startsWith("public class $dtoType") || trimmedLine.startsWith("class $dtoType")) {
                    inTargetClass = true
                    targetClassName = dtoType
                    println("🔍 Java class 발견: $trimmedLine")
                    continue
                }
                
                // 다른 클래스 시작 시 현재 클래스 종료
                if (trimmedLine.startsWith("data class ") && !trimmedLine.startsWith("data class $dtoType")) {
                    inTargetClass = false
                    targetClassName = ""
                }
                if (trimmedLine.startsWith("public class ") && !trimmedLine.startsWith("public class $dtoType")) {
                    inTargetClass = false
                    targetClassName = ""
                }
                if (trimmedLine.startsWith("class ") && !trimmedLine.startsWith("class $dtoType")) {
                    inTargetClass = false
                    targetClassName = ""
                }
                
                // 필드 파싱 (타겟 클래스 내에서만)
                if (inTargetClass && targetClassName == dtoType) {
                    if (trimmedLine.startsWith("val ") || trimmedLine.startsWith("var ")) {
                        val fieldInfo = parseKotlinField(trimmedLine)
                        if (fieldInfo != null) {
                            fields.add(fieldInfo)
                            println("🔍 Kotlin 필드 발견: ${fieldInfo.name}: ${fieldInfo.type}")
                        }
                    } else if (trimmedLine.startsWith("private ") || trimmedLine.startsWith("public ")) {
                        val fieldInfo = parseJavaField(trimmedLine)
                        if (fieldInfo != null) {
                            fields.add(fieldInfo)
                            println("🔍 Java 필드 발견: ${fieldInfo.name}: ${fieldInfo.type}")
                        }
                    }
                    
                    // 클래스 끝 확인
                    if (trimmedLine == "}" || trimmedLine.startsWith("}")) {
                        break
                    }
                }
            }
        }
        
        println("🔍 총 필드 수: ${fields.size}")
        return fields
    }
    
    private fun parseKotlinField(line: String): FieldInfo? {
        val match = Regex("""(val|var)\s+([a-zA-Z][a-zA-Z0-9]*)\s*:\s*([A-Za-z0-9.<>?]+)""").find(line)
        if (match != null) {
            val name = match.groupValues[2]
            val type = match.groupValues[3]
            val nullable = type.endsWith("?")
            val cleanType = if (nullable) type.substringBefore("?") else type
            return FieldInfo(name, cleanType, nullable, null)
        }
        return null
    }
    
    private fun parseJavaField(line: String): FieldInfo? {
        val match = Regex("""(private|public)\s+([A-Za-z0-9.<>]+)\s+([a-zA-Z][a-zA-Z0-9]*)\s*;""").find(line)
        if (match != null) {
            val type = match.groupValues[2]
            val name = match.groupValues[3]
            return FieldInfo(name, type, false, null) // Java에서는 기본적으로 nullable이 아님
        }
        return null
    }
    
    private fun parseKotlinParameters(paramSection: String): List<FieldInfo> {
        val fields = mutableListOf<FieldInfo>()
        val params = paramSection.split(",").map { it.trim() }
        
        params.forEach { param ->
            if (param.isNotEmpty()) {
                // 기본 파라미터 파싱: name: String
                val paramMatch = Regex("([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9.<>?]+)").find(param)
                if (paramMatch != null) {
                    val fieldName = paramMatch.groupValues[1]
                    val fieldType = paramMatch.groupValues[2]
                    val nullable = fieldType.endsWith("?")
                    val cleanType = if (nullable) fieldType.substringBefore("?") else fieldType
                    
                    fields.add(FieldInfo(
                        name = fieldName,
                        type = cleanType,
                        nullable = nullable,
                        description = null
                    ))
                } else {
                    // 기본값이 있는 파라미터 파싱: name: String = "default"
                    val defaultMatch = Regex("([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9.<>?]+)\\s*=\\s*([^,]+)").find(param)
                    if (defaultMatch != null) {
                        val fieldName = defaultMatch.groupValues[1]
                        val fieldType = defaultMatch.groupValues[2]
                        val nullable = fieldType.endsWith("?")
                        val cleanType = if (nullable) fieldType.substringBefore("?") else fieldType
                        
                        fields.add(FieldInfo(
                            name = fieldName,
                            type = cleanType,
                            nullable = nullable,
                            description = null
                        ))
                    }
                }
            }
        }
        
        return fields
    }
    
    private fun parseJavaDtoFields(content: String, dtoType: String): List<FieldInfo> {
        val fields = mutableListOf<FieldInfo>()
        val lines = content.lines()
        
        var inTargetClass = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // 타겟 클래스 찾기
            if (trimmedLine.startsWith("public class $dtoType") || 
                trimmedLine.startsWith("class $dtoType")) {
                inTargetClass = true
            } else if (inTargetClass && trimmedLine.startsWith("private ")) {
                // Java 필드 파싱
                val fieldMatch = Regex("private\\s+([A-Za-z0-9<>]+)\\s+([a-zA-Z][a-zA-Z0-9]*)\\s*;").find(trimmedLine)
                if (fieldMatch != null) {
                    val fieldType = fieldMatch.groupValues[1]
                    val fieldName = fieldMatch.groupValues[2]
                    
                    fields.add(FieldInfo(
                        name = fieldName,
                        type = fieldType,
                        nullable = false, // Java에서는 기본적으로 nullable이 아님
                        description = null
                    ))
                }
            } else if (inTargetClass && trimmedLine.startsWith("}")) {
                break
            }
        }
        
        return fields
    }
    
    private fun findDtoFiles(dtoType: String): List<File> {
        val dtoFiles = mutableListOf<File>()
        // 모든 모듈의 모든 .kt, .java 파일을 대상으로 한다
        file(".").listFiles()?.filter { it.isDirectory && file("${it.name}/build.gradle.kts").exists() }?.forEach { module ->
            val kotlinDtoPath = "${module.name}/src/main/kotlin"
            val javaDtoPath = "${module.name}/src/main/java"
            if (file(kotlinDtoPath).exists()) {
                file(kotlinDtoPath).walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .forEach { dtoFiles.add(it) }
            }
            if (file(javaDtoPath).exists()) {
                file(javaDtoPath).walkTopDown()
                    .filter { it.isFile && it.extension == "java" }
                    .forEach { dtoFiles.add(it) }
            }
        }
        
        return dtoFiles.filter { file ->
            // 파일 내용에 class 선언이 있는지 확인
            val content = file.readText()
            // Kotlin data class
            content.contains("data class $dtoType(") ||
            content.contains("class $dtoType(") ||
            // Java class
            content.contains("public class $dtoType") ||
            content.contains("class $dtoType ") ||
            // 제네릭 타입 처리 (List<Type> 형태)
            content.contains("class $dtoType<") ||
            content.contains("public class $dtoType<") ||
            // 패키지가 포함된 경우
            content.contains("class $dtoType") ||
            content.contains("public class $dtoType")
        }
    }

    // analyzeDtoFields에서 파일명과 무관하게 파일 내 선언부를 정확히 파싱하도록 보장 (기존 로직은 그대로 사용, 위에서 파일만 잘 찾으면 됨)
    
    private fun analyzeExceptionsInFunction(functionBody: String, startLine: Int): List<ExceptionInfo> {
        val exceptions = mutableListOf<ExceptionInfo>()
        val lines = functionBody.lines()
        
        for ((index, line) in lines.withIndex()) {
            val trimmedLine = line.trim()
            
            // throw 구문 찾기
            val throwPatterns = listOf(
                // 기본 throw 구문
                Regex("""throw\s+(\w+(?:\.\w+)*)(?:\(\))?(?:.*)?"""),
                // orElseThrow 패턴
                Regex("""\.orElseThrow\s*\{\s*(\w+(?:\.\w+)*)(?:\(\))?\s*\}"""),
                // throw with message
                Regex("""throw\s+(\w+(?:\.\w+)*)\s*\(\s*[""']([^""']*)[""']\s*\)""")
            )
            
            for (pattern in throwPatterns) {
                val match = pattern.find(trimmedLine)
                if (match != null) {
                    val exceptionType = match.groupValues[1]
                    val message = if (match.groupValues.size > 2) match.groupValues[2] else null
                    
                    val errorCode = generateErrorCode(exceptionType)
                    val errorResponse = generateErrorResponse(exceptionType, message)
                    
                    exceptions.add(ExceptionInfo(
                        exceptionType = exceptionType,
                        message = message,
                        errorCode = errorCode,
                        errorResponse = errorResponse
                    ))
                    
                    println("🔍 예외 발견: $exceptionType (에러 코드: $errorCode)")
                    break
                }
            }
        }
        
        return exceptions
    }
    
    private fun analyzeJavaExceptionsInFile(file: File): List<ExceptionInfo> {
        val exceptions = mutableListOf<ExceptionInfo>()
        val content = file.readText()
        val lines = content.lines()
        
        for ((lineIndex, line) in lines.withIndex()) {
            val trimmedLine = line.trim()
            
            // Java 스타일 throw 구문 찾기
            val javaThrowPatterns = listOf(
                // throw new ExceptionType("message")
                Regex("""throw\s+new\s+(\w+(?:\.\w+)*)\s*\(\s*[""']([^""']*)[""']\s*\)"""),
                // throw new ExceptionType()
                Regex("""throw\s+new\s+(\w+(?:\.\w+)*)\s*\(\s*\)"""),
                // throw new ExceptionType(변수)
                Regex("""throw\s+new\s+(\w+(?:\.\w+)*)\s*\([^)]*\)"""),
                // throw exceptionVariable
                Regex("""throw\s+(\w+(?:\.\w+)*)""")
            )
            
            for (pattern in javaThrowPatterns) {
                val match = pattern.find(trimmedLine)
                if (match != null) {
                    val exceptionType = match.groupValues[1]
                    val message = if (match.groupValues.size > 2) match.groupValues[2] else null
                    
                    val errorCode = generateErrorCode(exceptionType)
                    val errorResponse = generateErrorResponse(exceptionType, message)
                    
                    exceptions.add(ExceptionInfo(
                        exceptionType = exceptionType,
                        message = message,
                        errorCode = errorCode,
                        errorResponse = errorResponse
                    ))
                    
                    println("🔍 Java 예외 발견: $exceptionType (에러 코드: $errorCode)")
                    break
                }
            }
        }
        
        return exceptions
    }
    
    private fun analyzeServiceExceptions(controllerName: String, module: String): List<ExceptionInfo> {
        val exceptions = mutableListOf<ExceptionInfo>()
        
        // 비즈니스 로직 파일 찾기
        val businessFiles = findBusinessLogicFiles(controllerName, module)
        
        for (file in businessFiles) {
            when (file.extension) {
                "java" -> {
                    // Java 파일은 AST 기반 분석
                    val javaExceptions = analyzeJavaExceptionsInFile(file)
                    exceptions.addAll(javaExceptions)
                }
                "kt" -> {
                    // Kotlin 파일은 기존 정규식 분석
                    val content = file.readText()
                    val lines = content.lines()
                    
                    for ((lineIndex, line) in lines.withIndex()) {
                        val trimmedLine = line.trim()
                        
                        // throw 구문 찾기
                        val throwPatterns = listOf(
                            Regex("""throw\s+(\w+(?:\.\w+)*)(?:\(\))?(?:.*)?"""),
                            Regex("""\.orElseThrow\s*\{\s*(\w+(?:\.\w+)*)(?:\(\))?\s*\}"""),
                            Regex("""throw\s+(\w+(?:\.\w+)*)\s*\(\s*[""']([^""']*)[""']\s*\)""")
                        )
                        
                        for (pattern in throwPatterns) {
                            val match = pattern.find(trimmedLine)
                            if (match != null) {
                                val exceptionType = match.groupValues[1]
                                val message = if (match.groupValues.size > 2) match.groupValues[2] else null
                                
                                val errorCode = generateErrorCode(exceptionType)
                                val errorResponse = generateErrorResponse(exceptionType, message)
                                
                                exceptions.add(ExceptionInfo(
                                    exceptionType = exceptionType,
                                    message = message,
                                    errorCode = errorCode,
                                    errorResponse = errorResponse
                                ))
                                
                                println("🔍 Kotlin 예외 발견: $exceptionType (에러 코드: $errorCode)")
                                break
                            }
                        }
                    }
                }
            }
        }
        
        return exceptions
    }
    
    private fun findBusinessLogicFiles(controllerName: String, module: String): List<File> {
        val businessFiles = mutableListOf<File>()
        val businessKeywords = listOf("Service", "Manager", "Processor", "Analyzer", "Engine", "Orchestrator")
        val serviceDirKotlin = File("$module/src/main/kotlin")
        val serviceDirJava = File("$module/src/main/java")

        if (serviceDirKotlin.exists()) {
            serviceDirKotlin.walkTopDown()
                .filter { it.isFile && it.extension in listOf("kt", "java") }
                .filter { file -> businessKeywords.any { keyword -> file.name.contains(keyword) } }
                .forEach { businessFiles.add(it) }
        }
        if (serviceDirJava.exists()) {
            serviceDirJava.walkTopDown()
                .filter { it.isFile && it.extension in listOf("kt", "java") }
                .filter { file -> businessKeywords.any { keyword -> file.name.contains(keyword) } }
                .forEach { businessFiles.add(it) }
        }
        return businessFiles
    }
    
    private fun generateErrorCode(exceptionType: String): String {
        return when (exceptionType) {
            "UserNotFoundException" -> "404"
            "BookingNotFoundException" -> "404"
            "StockNotFoundException" -> "404"
            "ValidationException" -> "400"
            "UnauthorizedException" -> "401"
            "ForbiddenException" -> "403"
            "ConflictException" -> "409"
            "InternalServerException" -> "500"
            else -> "500"
        }
    }
    
    private fun generateErrorResponse(exceptionType: String, message: String?): String {
        val defaultMessage = when (exceptionType) {
            "UserNotFoundException" -> "사용자를 찾을 수 없습니다"
            "BookingNotFoundException" -> "예약을 찾을 수 없습니다"
            "StockNotFoundException" -> "재고를 찾을 수 없습니다"
            "ValidationException" -> "잘못된 요청입니다"
            "UnauthorizedException" -> "인증이 필요합니다"
            "ForbiddenException" -> "접근 권한이 없습니다"
            "ConflictException" -> "리소스 충돌이 발생했습니다"
            "InternalServerException" -> "서버 내부 오류가 발생했습니다"
            else -> "알 수 없는 오류가 발생했습니다"
        }
        
        return message ?: defaultMessage
    }
    
    private fun generateJson(controllers: List<ControllerInfo>): String {
        // 간단한 JSON 생성 (Jackson 의존성 없이)
        return controllers.joinToString(",") { controller ->
            """
            {
                "module": "${controller.module}",
                "fileName": "${controller.fileName}",
                "className": "${controller.className}",
                "basePath": "${controller.basePath}",
                "endpoints": [
                    ${controller.endpoints.joinToString(",") { endpoint ->
                        """
                        {
                            "method": "${endpoint.method}",
                            "path": "${endpoint.path}",
                            "requestType": "${endpoint.requestType}",
                            "responseType": "${endpoint.responseType}",
                            "exceptions": [
                                ${endpoint.exceptions.joinToString(",") { exception ->
                                    """
                                    {
                                        "exceptionType": "${exception.exceptionType}",
                                        "message": "${exception.message ?: ""}",
                                        "errorCode": "${exception.errorCode}",
                                        "errorResponse": "${exception.errorResponse}"
                                    }
                                    """.trimIndent()
                                }}
                            ]
                        }
                        """.trimIndent()
                    }}
                ]
            }
            """.trimIndent()
        }.let { "[$it]" }
    }
    
    private fun generateHtml(json: String, controllers: List<ControllerInfo>, outputFile: String) {
        val html = """
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>API Documentation</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #1a1a1a;
            color: #ffffff;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        .header {
            background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            text-align: center;
            border: 1px solid #34495e;
        }
        .header h1 {
            margin: 0;
            font-size: 2.5em;
            font-weight: 300;
        }
        .header p {
            margin: 10px 0 0 0;
            opacity: 0.9;
            font-size: 1.1em;
        }
        .controller {
            background: #2d2d2d;
            border-radius: 10px;
            margin-bottom: 30px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
            overflow: hidden;
            border: 1px solid #404040;
        }
        .controller-header {
            background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
            color: white;
            padding: 20px;
            cursor: pointer;
            user-select: none;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .controller-header:hover {
            background: linear-gradient(135deg, #2980b9 0%, #1f5f8b 100%);
        }
        .controller-header-content {
            flex: 1;
        }
        .controller-header h2 {
            margin: 0;
            font-size: 1.5em;
            font-weight: 400;
        }
        .controller-header .module {
            opacity: 0.8;
            font-size: 0.9em;
            margin-top: 5px;
        }
        .controller-content {
            transition: all 0.3s ease;
        }
        .controller-content {
            display: none;
        }
        .controller.expanded .controller-content {
            display: block;
        }
        .toggle-icon {
            font-size: 1.2em;
            transition: transform 0.3s ease;
            color: rgba(255, 255, 255, 0.8);
        }
        .controller .toggle-icon {
            transform: rotate(-90deg);
        }
        .controller.expanded .toggle-icon {
            transform: rotate(0deg);
        }
        .endpoints {
            padding: 20px;
        }
        .endpoint {
            border: 1px solid #404040;
            border-radius: 8px;
            margin-bottom: 20px;
            overflow: hidden;
            background: #333333;
        }
        .endpoint-header {
            background: #404040;
            padding: 15px;
            border-bottom: 1px solid #505050;
            display: flex;
            align-items: center;
            gap: 15px;
            cursor: pointer;
            user-select: none;
        }
        .endpoint-header:hover {
            background: #505050;
        }
        .endpoint-content {
            transition: all 0.3s ease;
        }
        .endpoint-content {
            display: none;
        }
        .endpoint.expanded .endpoint-content {
            display: block;
        }
        .endpoint-toggle-icon {
            font-size: 1em;
            transition: transform 0.3s ease;
            color: rgba(255, 255, 255, 0.8);
            margin-left: 10px;
        }
        .endpoint .endpoint-toggle-icon {
            transform: rotate(-90deg);
        }
        .endpoint.expanded .endpoint-toggle-icon {
            transform: rotate(0deg);
        }
        .method {
            padding: 5px 12px;
            border-radius: 4px;
            font-weight: bold;
            font-size: 0.9em;
            text-transform: uppercase;
        }
        .method.get { background-color: #27ae60; color: white; }
        .method.post { background-color: #3498db; color: white; }
        .method.put { background-color: #f39c12; color: black; }
        .method.delete { background-color: #e74c3c; color: white; }
        .method.patch { background-color: #9b59b6; color: white; }
        .path {
            font-family: 'Courier New', monospace;
            font-size: 1.1em;
            color: #ecf0f1;
        }
        .details {
            padding: 20px;
        }
        .detail-item {
            margin-bottom: 15px;
        }
        .detail-label {
            font-weight: bold;
            color: #3498db;
            margin-right: 10px;
        }
        .field-list {
            background: #2c3e50;
            border-radius: 6px;
            padding: 15px;
            margin-top: 10px;
            font-family: 'Courier New', monospace;
        }
        .field code {
            color: #ecf0f1; // Only color, no background
        }
        .exceptions {
            margin-top: 10px;
        }
        .exception {
            background: #8B0000;
            color: #ffcccc;
            padding: 12px;
            border-radius: 6px;
            margin: 8px 0;
            border-left: 4px solid #ff4444;
        }
        .exception-header {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 5px;
        }
        .error-code {
            background: #ff4444;
            color: white;
            padding: 2px 8px;
            border-radius: 3px;
            font-weight: bold;
            font-size: 0.8em;
            min-width: 40px;
            text-align: center;
        }
        .exception-type {
            font-family: 'Courier New', monospace;
            font-weight: bold;
            color: #ffcccc;
        }
        .error-message {
            color: #ffaaaa;
            font-size: 0.9em;
            margin-top: 3px;
        }
    </style>
    <script>
        function toggleController(controllerId) {
            const controller = document.getElementById(controllerId);
            controller.classList.toggle('expanded');
        }
        
        function toggleEndpoint(endpointId) {
            const endpoint = document.getElementById(endpointId);
            endpoint.classList.toggle('expanded');
        }
    </script>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🚀 API Documentation</h1>
            <p>Spring Boot Controller 분석 결과</p>
        </div>
        
        ${controllers.joinToString("\n") { controller ->
            val controllerId = "controller-${controller.className.lowercase()}"
            """
            <div class="controller" id="$controllerId">
                <div class="controller-header" onclick="toggleController('$controllerId')">
                    <div class="controller-header-content">
                        <h2>${controller.className}</h2>
                        <div class="module">📦 Module: ${controller.module} | 📄 File: ${controller.fileName}</div>
                    </div>
                    <div class="toggle-icon">▼</div>
                </div>
                <div class="controller-content">
                    <div class="endpoints">
                        ${controller.endpoints.mapIndexed { index, endpoint ->
                            val endpointId = "endpoint-${controller.className.lowercase()}-$index"
                            """
                            <div class="endpoint" id="$endpointId">
                                <div class="endpoint-header" onclick="toggleEndpoint('$endpointId')">
                                    <div style="display: flex; align-items: center; flex: 1;">
                                        <span class="method ${endpoint.method.lowercase()}">${endpoint.method}</span>
                                        <span class="path">${endpoint.path}</span>
                                    </div>
                                    <div class="endpoint-toggle-icon">▼</div>
                                </div>
                                <div class="endpoint-content">
                                    <div class="details">
                                        ${if (endpoint.requestDetails.pathVariables.isNotEmpty()) "<div class='detail-item'><span class='detail-label'>🔗 Path Variables:</span> <code>${endpoint.requestDetails.pathVariables.joinToString(", ")}</code></div>" else ""}
                                        ${if (endpoint.requestType.isNotEmpty()) {
                                            val requestFields = analyzeDtoFields(endpoint.requestType)
                                            if (requestFields.isNotEmpty()) {
                                                """
                                                <div class='detail-item'>
                                                    <span class='detail-label'>📦 Request Model:</span>
                                                    <div class='request-fields'>
                                                        <div class='field-list'>
                                                            <div class='field'><code>{</code></div>
                                                            ${requestFields.mapIndexed { index, field ->
                                                                val nullableMark = if (field.nullable) "?" else ""
                                                                val comma = if (index < requestFields.size - 1) "," else ""
                                                                "<div class='field'><code>&nbsp;&nbsp;\"${field.name}\": ${field.type}$nullableMark$comma</code></div>"
                                                            }.joinToString("<br>")}
                                                            <div class='field'><code>}</code></div>
                                                        </div>
                                                    </div>
                                                </div>
                                                """
                                            } else ""
                                        } else ""}
                                        ${if (endpoint.responseDetails.fields.isNotEmpty()) {
                                            """
                                            <div class='detail-item'>
                                                <span class='detail-label'>📤 Response Model:</span>
                                                <div class='response-fields'>
                                                    <div class='field-list'>
                                                        <div class='field'><code>{</code></div>
                                                        ${endpoint.responseDetails.fields.mapIndexed { index, field ->
                                                            val nullableMark = if (field.nullable) "?" else ""
                                                            val comma = if (index < endpoint.responseDetails.fields.size - 1) "," else ""
                                                            "<div class='field'><code>&nbsp;&nbsp;\"${field.name}\": ${field.type}$nullableMark$comma</code></div>"
                                                        }.joinToString("<br>")}
                                                        <div class='field'><code>}</code></div>
                                                    </div>
                                                </div>
                                            </div>
                                            """
                                        } else ""}
                                        ${if (endpoint.exceptions.isNotEmpty()) {
                                            """
                                            <div class='detail-item'>
                                                <span class='detail-label'>⚠️ Exceptions:</span>
                                                <div class='exceptions'>
                                                    ${endpoint.exceptions.map { exception ->
                                                        """
                                                        <div class='exception'>
                                                            <div class='exception-header'>
                                                                <span class='error-code'>${exception.errorCode}</span>
                                                                <span class='exception-type'>${exception.exceptionType}</span>
                                                            </div>
                                                            <div class='error-message'>${exception.errorResponse}</div>
                                                        </div>
                                                        """
                                                    }.joinToString("")}
                                                </div>
                                            </div>
                                            """
                                        } else ""}
                                    </div>
                                </div>
                            </div>
                            """
                        }.joinToString("\n")}
                    </div>
                </div>
            </div>
            """
        }}
    </div>
</body>
</html>
        """.trimIndent()
        
        file(outputFile).writeText(html)
    }
}

// 데이터 클래스들
data class ControllerInfo(
    val module: String,
    val fileName: String,
    val className: String,
    val basePath: String,
    val endpoints: List<EndpointInfo>
)

data class EndpointInfo(
    val method: String,
    val path: String,
    val requestType: String,
    val responseType: String,
    val requestDetails: RequestDetails,
    val responseDetails: ResponseDetails,
    val exceptions: List<ExceptionInfo>
)

data class RequestDetails(
    val parameters: List<ParameterInfo>,
    val bodyType: String?,
    val pathVariables: List<String>
)

data class ResponseDetails(
    val type: String,
    val isList: Boolean,
    val genericType: String?,
    val fields: List<FieldInfo>
)

data class FieldInfo(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val description: String?
)

data class ParameterInfo(
    val name: String,
    val type: String,
    val annotation: String?,
    val required: Boolean
)

data class ExceptionInfo(
    val exceptionType: String,
    val message: String?,
    val errorCode: String,
    val errorResponse: String
)
