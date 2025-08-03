import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.3.2" apply true
    id("io.spring.dependency-management") version "1.1.6" apply true
    kotlin("jvm") version "1.9.24" apply true
    kotlin("plugin.spring") version "1.9.24" apply true
}

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
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

// AST 분석을 위한 의존성 추가
configurations.create("analysis") {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    "analysis"("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.24")
    "analysis"("org.jetbrains.kotlin:kotlin-compiler-internal-test-framework:1.9.24")
    "analysis"("com.fasterxml.jackson.core:jackson-databind")
    "analysis"("com.fasterxml.jackson.module:jackson-module-kotlin")
}

// AST 분석 Task 정의
tasks.register("apiDocuments") {
    group = "analysis"
    description = "Analyze Spring Boot controllers using AST and generate HTML documentation"
    
    doLast {
        val analyzer = ControllerAnalyzer()
        analyzer.analyze()
    }
}

// 설정 가능한 옵션들
val analysisConfig = mapOf(
    "outputFile" to "build/controller-docs.html",
    "includeJava" to true,
    "includeKotlin" to true,
    "excludeDirs" to listOf("build", ".gradle", ".git", ".idea", "node_modules"),
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
                        if (trimmedLine.startsWith("public class ") && trimmedLine.contains("Controller")) {
                            currentClass = trimmedLine.substringAfter("public class ").substringBefore("(").substringBefore(":").substringBefore(" {")
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
        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("fun ") || line.startsWith("public ")) {
                // 메소드명 제거 - 빈 문자열 반환
                
                // Request 타입 찾기
                val requestType = extractRequestType(line)
                
                // Response 타입 찾기
                val responseType = extractResponseType(line)
                
                return Pair(requestType, responseType)
            }
        }
        return Pair( "", "")
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
        // @RequestBody request: CreateBookingRequest 형태 파싱
        if (functionLine.contains("@RequestBody")) {
            val requestBodyMatch = Regex("@RequestBody\\s+([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9<>]+)").find(functionLine)
            if (requestBodyMatch != null) {
                return requestBodyMatch.groupValues[2] // 타입 부분 반환
            }
            
            // @RequestBody CreateBookingRequest request 형태 파싱
            val simpleMatch = Regex("@RequestBody\\s+([A-Za-z0-9<>]+)").find(functionLine)
            if (simpleMatch != null) {
                return simpleMatch.groupValues[1]
            }
        }
        
        // @PathVariable id: Long 형태 파싱
        if (functionLine.contains("@PathVariable")) {
            val pathVariableMatch = Regex("@PathVariable\\s+([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9<>]+)").find(functionLine)
            if (pathVariableMatch != null) {
                return pathVariableMatch.groupValues[2] // 타입 부분 반환
            }
            
            // @PathVariable Long id 형태 파싱
            val simplePathVariableMatch = Regex("@PathVariable\\s+([A-Za-z0-9<>]+)").find(functionLine)
            if (simplePathVariableMatch != null) {
                return simplePathVariableMatch.groupValues[1]
            }
        }
        
        return ""
    }
    
    private fun extractResponseType(functionLine: String): String {
        // 간단한 문자열 파싱으로 변경
        if (functionLine.contains("ResponseEntity<")) {
            val start = functionLine.indexOf("ResponseEntity<") + "ResponseEntity<".length
            val end = functionLine.indexOf(">", start)
            if (start > 0 && end > start) {
                return functionLine.substring(start, end)
            }
        }
        
        if (functionLine.contains(":")) {
            val parts = functionLine.split(":")
            if (parts.size > 1) {
                val returnType = parts[1].trim().split("\\s+".toRegex()).firstOrNull()
                return returnType ?: ""
            }
        }
        
        return ""
    }
    
    private fun analyzeRequestDetails(lines: List<String>, startIndex: Int): RequestDetails {
        val parameters = mutableListOf<ParameterInfo>()
        val pathVariables = mutableListOf<String>()
        var bodyType: String? = null
        
        // 함수 정의 라인 찾기 (여러 줄에 걸친 경우도 처리)
        var functionSignature = ""
        var braceCount = 0
        var inFunction = false
        
        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            
            if (line.startsWith("fun ") || line.startsWith("public ")) {
                inFunction = true
                functionSignature = line
                braceCount = line.count { it == '(' } - line.count { it == ')' }
                
                if (braceCount == 0) {
                    // 한 줄에 완성된 함수 시그니처
                    break
                }
            } else if (inFunction) {
                functionSignature += " " + line
                braceCount += line.count { it == '(' } - line.count { it == ')' }
                
                if (braceCount == 0) {
                    break
                }
            }
        }
        
        if (functionSignature.isNotEmpty()) {
            // 파라미터 부분 추출
            val paramSection = extractParameterSection(functionSignature)
            if (paramSection.isNotEmpty()) {
                val paramList = parseParameters(paramSection)
                parameters.addAll(paramList)
                
                // @RequestBody 타입 찾기 - 더 정확한 방법으로 개선
                val requestBodyParam = paramList.find { it.annotation == "@RequestBody" }
                if (requestBodyParam != null) {
                    bodyType = requestBodyParam.type
                    // 타입이 Unknown인 경우 함수 시그니처에서 다시 찾기
                    if (bodyType == "Unknown") {
                        bodyType = findRequestBodyTypeFromSignature(functionSignature, requestBodyParam.name)
                    }
                }
                
                // @PathVariable 찾기
                pathVariables.addAll(paramList.filter { it.annotation == "@PathVariable" }.map { it.name })
            }
        }
        
        return RequestDetails(
            parameters = parameters,
            bodyType = bodyType,
            pathVariables = pathVariables
        )
    }
    
    private fun findRequestBodyTypeFromSignature(signature: String, paramName: String): String? {
        // @RequestBody request: CreateUserRequest 형태 찾기
        val typeMatch = Regex("@RequestBody\\s+$paramName\\s*:\\s*([A-Za-z0-9<>]+)").find(signature)
        if (typeMatch != null) {
            return typeMatch.groupValues[1]
        }
        
        // CreateUserRequest request 형태 찾기
        val typeNameMatch = Regex("([A-Za-z0-9<>]+)\\s+$paramName").find(signature)
        if (typeNameMatch != null) {
            return typeNameMatch.groupValues[1]
        }
        
        return null
    }
    
    private fun extractParameterSection(functionSignature: String): String {
        val startIndex = functionSignature.indexOf('(')
        val endIndex = functionSignature.indexOf(')')
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return functionSignature.substring(startIndex + 1, endIndex)
        }
        
        return ""
    }
    
    private fun parseParameters(paramSection: String): List<ParameterInfo> {
        val parameters = mutableListOf<ParameterInfo>()
        
        // 파라미터들을 쉼표로 분리
        val paramStrings = paramSection.split(",").map { it.trim() }
        
        paramStrings.forEach { param ->
            if (param.isNotEmpty()) {
                val paramInfo = parseSingleParameter(param)
                if (paramInfo != null) {
                    parameters.add(paramInfo)
                }
            }
        }
        
        return parameters
    }
    
    private fun parseSingleParameter(param: String): ParameterInfo? {
        // @RequestBody CreateUserRequest request
        // @PathVariable id: Long
        // request: CreateUserRequest
        // Long id
        
        // @PathVariable id: Long 형태
        val pathVariableMatch = Regex("@PathVariable\\s+([a-z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9<>]+)").find(param)
        if (pathVariableMatch != null) {
            return ParameterInfo(
                name = pathVariableMatch.groupValues[1],
                type = pathVariableMatch.groupValues[2],
                annotation = "@PathVariable",
                required = true
            )
        }
        
        // @RequestBody request: CreateBookingRequest 형태
        val requestBodyWithTypeMatch = Regex("@RequestBody\\s+([a-z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9<>]+)").find(param)
        if (requestBodyWithTypeMatch != null) {
            return ParameterInfo(
                name = requestBodyWithTypeMatch.groupValues[1],
                type = requestBodyWithTypeMatch.groupValues[2],
                annotation = "@RequestBody",
                required = true
            )
        }
        
        // @RequestBody CreateUserRequest request 형태
        val requestBodyMatch = Regex("@RequestBody\\s+([A-Za-z0-9<>]+)\\s+([a-z][a-zA-Z0-9]*)").find(param)
        if (requestBodyMatch != null) {
            return ParameterInfo(
                name = requestBodyMatch.groupValues[2],
                type = requestBodyMatch.groupValues[1],
                annotation = "@RequestBody",
                required = true
            )
        }
        
        // @RequestBody request 형태 (타입이 별도로 있는 경우)
        val requestBodySimpleMatch = Regex("@RequestBody\\s+([a-z][a-zA-Z0-9]*)").find(param)
        if (requestBodySimpleMatch != null) {
            return ParameterInfo(
                name = requestBodySimpleMatch.groupValues[1],
                type = "Unknown",
                annotation = "@RequestBody",
                required = true
            )
        }
        
        // Long id 형태
        val typeNameMatch = Regex("([A-Za-z0-9<>]+)\\s+([a-z][a-zA-Z0-9]*)").find(param)
        if (typeNameMatch != null) {
            return ParameterInfo(
                name = typeNameMatch.groupValues[2],
                type = typeNameMatch.groupValues[1],
                annotation = null,
                required = true
            )
        }
        
        return null
    }
    
    private fun analyzeResponseDetails(responseType: String): ResponseDetails {
        val isList = responseType.startsWith("List<") || responseType.startsWith("Array<")
        val genericType = if (isList) {
            responseType.substringAfter("<").substringBefore(">")
        } else null
        
        // 실제 타입 (List 제거)
        val actualType = if (isList) genericType else responseType
        
        // DTO 클래스의 필드 정보 분석
        val fields = analyzeDtoFields(actualType)
        
        return ResponseDetails(
            type = responseType,
            isList = isList,
            genericType = genericType,
            fields = fields
        )
    }
    
    private fun analyzeDtoFields(dtoType: String?): List<FieldInfo> {
        if (dtoType == null) return emptyList()
        
        val fields = mutableListOf<FieldInfo>()
        
        // 각 모듈에서 DTO 파일 찾기
        listOf("user", "booking", "stock").forEach { module ->
            val dtoPath = "$module/src/main/kotlin"
            val javaDtoPath = "$module/src/main/java"
            
            // Kotlin DTO 파일 찾기
            if (file(dtoPath).exists()) {
                file(dtoPath).walkTopDown()
                    .filter { it.extension == "kt" }
                    .forEach { file ->
                        val content = file.readText()
                        if (content.contains(dtoType)) {
                            fields.addAll(parseKotlinDtoFields(content, dtoType))
                        }
                    }
            }
            
            // Java DTO 파일 찾기
            if (file(javaDtoPath).exists()) {
                file(javaDtoPath).walkTopDown()
                    .filter { it.extension == "java" }
                    .forEach { file ->
                        val content = file.readText()
                        if (content.contains(dtoType)) {
                            fields.addAll(parseJavaDtoFields(content, dtoType))
                        }
                    }
            }
        }
        
        return fields
    }
    
    private fun parseKotlinDtoFields(content: String, dtoType: String): List<FieldInfo> {
        val fields = mutableListOf<FieldInfo>()
        val lines = content.lines()
        
        var inTargetClass = false
        var inDataClass = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // 타겟 클래스 찾기
            if (trimmedLine.startsWith("data class $dtoType(") || 
                trimmedLine.startsWith("class $dtoType(")) {
                inTargetClass = true
                inDataClass = trimmedLine.startsWith("data class")
                
                // 한 줄에 완성된 경우
                if (trimmedLine.endsWith(")")) {
                    val paramSection = trimmedLine.substringAfter("(").substringBefore(")")
                    if (paramSection.isNotEmpty()) {
                        fields.addAll(parseKotlinParameters(paramSection))
                    }
                    break
                }
            } else if (inTargetClass && trimmedLine.startsWith("val ")) {
                // 멀티라인 파라미터
                val fieldMatch = Regex("val\\s+([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9<>?]+)").find(trimmedLine)
                if (fieldMatch != null) {
                    val fieldName = fieldMatch.groupValues[1]
                    val fieldType = fieldMatch.groupValues[2]
                    val nullable = fieldType.endsWith("?")
                    val cleanType = if (nullable) fieldType.substringBefore("?") else fieldType
                    
                    fields.add(FieldInfo(
                        name = fieldName,
                        type = cleanType,
                        nullable = nullable,
                        description = null
                    ))
                }
            } else if (inTargetClass && trimmedLine == ")") {
                break
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
    
    private fun parseKotlinParameters(paramSection: String): List<FieldInfo> {
        val fields = mutableListOf<FieldInfo>()
        val params = paramSection.split(",").map { it.trim() }
        
        params.forEach { param ->
            if (param.isNotEmpty()) {
                val paramMatch = Regex("([a-zA-Z][a-zA-Z0-9]*)\\s*:\\s*([A-Za-z0-9<>?]+)").find(param)
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
                }
            }
        }
        
        return fields
    }
    

    
    private fun generateJson(controllers: List<ControllerInfo>): String {
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        return mapper.writeValueAsString(controllers)
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
            color: #ecf0f1;
            display: block;
            margin-bottom: 8px;
        }
        .parameter-list {
            background: #404040;
            padding: 10px;
            border-radius: 4px;
            border-left: 4px solid #3498db;
        }
        .parameter {
            margin-bottom: 5px;
        }
        .parameter:last-child {
            margin-bottom: 0;
        }
        .annotation {
            color: #3498db;
            font-weight: bold;
        }
        .request-fields, .response-fields {
            background: #404040;
            padding: 15px;
            border-radius: 6px;
            border-left: 4px solid #27ae60;
        }
        .field-list {
            font-family: 'Courier New', monospace;
        }
        .field {
            margin-bottom: 8px;
            padding: 5px 0;
        }
        .field:last-child {
            margin-bottom: 0;
        }
        .field code {
            color: #ecf0f1;
        }
        .json-data {
            background: #2d2d2d;
            border-radius: 10px;
            padding: 20px;
            margin-top: 30px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
            border: 1px solid #404040;
        }
        .json-data strong {
            color: #ecf0f1;
            font-size: 1.2em;
            margin-bottom: 15px;
            display: block;
        }
        pre {
            background: #1a1a1a;
            padding: 15px;
            border-radius: 6px;
            overflow-x: auto;
            font-family: 'Courier New', monospace;
            font-size: 0.9em;
            line-height: 1.4;
            border: 1px solid #404040;
            color: #ecf0f1;
        }
        code {
            color: #f39c12;
        }
        .controller-header {
            cursor: pointer;
            user-select: none;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .controller-header:hover {
            background: linear-gradient(135deg, #2980b9 0%, #1f5f8b 100%);
        }
        .controller-header-content {
            flex: 1;
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
            margin-left: 15px;
        }
        .controller .toggle-icon {
            transform: rotate(-90deg);
        }
        .controller.expanded .toggle-icon {
            transform: rotate(0deg);
        }
        .endpoint-header {
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
                                ${if (endpoint.requestDetails.bodyType != null) {
                                    val requestFields = analyzeDtoFields(endpoint.requestDetails.bodyType)
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
    
    private fun analyzeServiceExceptions(controllerName: String, module: String): List<ExceptionInfo> {
        val exceptions = mutableListOf<ExceptionInfo>()
        
        // Service 파일 찾기
        val businessFiles = findBusinessLogicFiles(controllerName, module)
        
        for (file in businessFiles) {
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
                        
                        println("🔍 Service 예외 발견: $exceptionType (에러 코드: $errorCode)")
                        break
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
