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
        
        var currentClass: String
        var currentBasePath = ""
        var inClass = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // 클래스 이름 찾기
            if (trimmedLine.startsWith("class ") && trimmedLine.contains("Controller")) {
                currentClass = trimmedLine.substringAfter("class ").substringBefore("(").substringBefore(":")
                className = currentClass
                inClass = true
            }
            
            // @RequestMapping 찾기
            if (trimmedLine.startsWith("@RequestMapping(") && inClass) {
                currentBasePath = extractPathFromAnnotation(trimmedLine)
                basePath = currentBasePath
            }
            
            // HTTP 메서드 어노테이션 찾기
            if (trimmedLine.startsWith("@GetMapping") || 
                trimmedLine.startsWith("@PostMapping") ||
                trimmedLine.startsWith("@PutMapping") ||
                trimmedLine.startsWith("@DeleteMapping") ||
                trimmedLine.startsWith("@PatchMapping")) {
                
                val endpoint = parseEndpoint(trimmedLine, lines, currentBasePath, lines.indexOf(line))
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
    
    private fun parseEndpoint(annotationLine: String, lines: List<String>, basePath: String, annotationIndex: Int): EndpointInfo? {
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
        val responseDetails = analyzeResponseDetails(functionSignature.third)
        
        return EndpointInfo(
            method = method,
            path = fullPath,
            functionName = functionSignature.first,
            requestType = functionSignature.second,
            responseType = functionSignature.third,
            requestDetails = requestDetails,
            responseDetails = responseDetails
        )
    }
    
    private fun findFunctionSignature(lines: List<String>, startIndex: Int): Triple<String, String, String> {
        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("fun ")) {
                val functionName = line.substringAfter("fun ").substringBefore("(")
                
                // Request 타입 찾기
                val requestType = extractRequestType(line)
                
                // Response 타입 찾기
                val responseType = extractResponseType(line)
                
                return Triple(functionName, requestType, responseType)
            }
        }
        return Triple("", "", "")
    }
    
    private fun extractRequestType(functionLine: String): String {
        // @RequestBody 파라미터 찾기
        val requestBodyMatch = Regex("@RequestBody\\s+([A-Za-z0-9<>]+)").find(functionLine)
        if (requestBodyMatch != null) {
            return requestBodyMatch.groupValues[1]
        }
        
        // @PathVariable 파라미터 찾기
        val pathVariableMatch = Regex("@PathVariable\\s+([A-Za-z0-9]+)").find(functionLine)
        if (pathVariableMatch != null) {
            return pathVariableMatch.groupValues[1]
        }
        
        // 일반 파라미터 찾기 (변수명이 아닌 타입을 찾기)
        val paramMatch = Regex("\\([^)]*\\b([A-Z][a-zA-Z0-9]*)\\s+[a-z][a-zA-Z0-9]*[^)]*\\)").find(functionLine)
        if (paramMatch != null) {
            return paramMatch.groupValues[1]
        }
        
        return ""
    }
    
    private fun extractResponseType(functionLine: String): String {
        // ResponseEntity<Type> 찾기
        val responseMatch = Regex("ResponseEntity<([A-Za-z0-9<>]+)>").find(functionLine)
        if (responseMatch != null) {
            return responseMatch.groupValues[1]
        }
        
        // 직접 반환 타입 찾기
        val directReturnMatch = Regex(":\\s*([A-Za-z0-9<>]+)\\s*\\{").find(functionLine)
        if (directReturnMatch != null) {
            return directReturnMatch.groupValues[1]
        }
        
        return ""
    }
    
    private fun analyzeRequestDetails(lines: List<String>, startIndex: Int): RequestDetails {
        val parameters = mutableListOf<ParameterInfo>()
        val pathVariables = mutableListOf<String>()
        var bodyType: String? = null
        
        // 함수 정의 라인 찾기
        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("fun ")) {
                // 파라미터 부분 추출
                val paramSection = line.substringAfter("(").substringBefore(")")
                if (paramSection.isNotEmpty()) {
                    val paramList = parseParameters(paramSection)
                    parameters.addAll(paramList)
                    
                    // @RequestBody 타입 찾기
                    bodyType = paramList.find { it.annotation == "@RequestBody" }?.type
                    
                    // @PathVariable 찾기
                    pathVariables.addAll(paramList.filter { it.annotation == "@PathVariable" }.map { it.name })
                }
                break
            }
        }
        
        return RequestDetails(
            parameters = parameters,
            bodyType = bodyType,
            pathVariables = pathVariables
        )
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
        
        val annotationMatch = Regex("(@[A-Za-z]+)\\s+([A-Za-z0-9<>]+)\\s+([a-z][a-zA-Z0-9]*)").find(param)
        if (annotationMatch != null) {
            return ParameterInfo(
                name = annotationMatch.groupValues[3],
                type = annotationMatch.groupValues[2],
                annotation = annotationMatch.groupValues[1],
                required = true
            )
        }
        
        // 타입 변수명 형태
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
        
        return ResponseDetails(
            type = responseType,
            isList = isList,
            genericType = genericType
        )
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
    <title>Spring Boot Controller API 문서</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        .header h1 {
            margin: 0;
            font-size: 2.5em;
            font-weight: 300;
        }
        .header p {
            margin: 10px 0 0 0;
            opacity: 0.9;
        }
        .content {
            padding: 30px;
        }
        .module {
            margin-bottom: 40px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            overflow: hidden;
        }
        .module-header {
            background-color: #f8f9fa;
            padding: 20px;
            border-bottom: 1px solid #e0e0e0;
        }
        .module-header h2 {
            margin: 0;
            color: #333;
            font-size: 1.5em;
        }
        .module-header .class-info {
            margin-top: 10px;
            color: #666;
            font-family: 'Courier New', monospace;
        }
        .endpoints {
            padding: 20px;
        }
        .endpoint {
            margin-bottom: 20px;
            padding: 15px;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            background-color: #fafafa;
        }
        .endpoint-header {
            display: flex;
            align-items: center;
            margin-bottom: 10px;
        }
        .method {
            padding: 4px 8px;
            border-radius: 4px;
            color: white;
            font-weight: bold;
            font-size: 0.8em;
            margin-right: 10px;
            min-width: 60px;
            text-align: center;
        }
        .method.get { background-color: #61affe; }
        .method.post { background-color: #49cc90; }
        .method.put { background-color: #fca130; }
        .method.delete { background-color: #f93e3e; }
        .method.patch { background-color: #50e3c2; }
        .path {
            font-family: 'Courier New', monospace;
            font-weight: bold;
            color: #333;
        }
        .function-name {
            color: #666;
            font-style: italic;
            margin-left: 10px;
        }
        .details {
            margin-top: 10px;
            font-size: 0.9em;
        }
        .detail-item {
            margin: 5px 0;
            color: #555;
        }
        .detail-label {
            font-weight: bold;
            color: #333;
        }
        .detail-item code {
            background-color: #f1f3f4;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
            font-size: 0.9em;
            color: #d73a49;
        }
        .parameter-list {
            margin-top: 5px;
            margin-left: 10px;
        }
        .parameter {
            margin: 2px 0;
            padding: 2px 0;
        }
        .annotation {
            color: #6f42c1;
            font-weight: bold;
        }
        .json-data {
            background-color: #f8f9fa;
            border: 1px solid #e0e0e0;
            border-radius: 4px;
            padding: 15px;
            margin-top: 20px;
            font-family: 'Courier New', monospace;
            font-size: 0.8em;
            white-space: pre-wrap;
            overflow-x: auto;
        }
        .stats {
            background-color: #e3f2fd;
            padding: 15px;
            border-radius: 6px;
            margin-bottom: 20px;
        }
        .stats h3 {
            margin: 0 0 10px 0;
            color: #1976d2;
        }
        .stats p {
            margin: 5px 0;
            color: #333;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔍 Spring Boot Controller API 문서</h1>
            <p>AST 분석을 통한 자동 생성된 API 문서</p>
        </div>
        
        <div class="content">
            <div class="stats">
                <h3>📊 분석 통계</h3>
                <p><strong>총 모듈 수:</strong> ${controllers.groupBy { it.module }.size}</p>
                <p><strong>총 Controller 수:</strong> ${controllers.size}</p>
                <p><strong>총 Endpoint 수:</strong> ${controllers.sumOf { it.endpoints.size }}</p>
            </div>
            
            ${controllers.groupBy { it.module }.map { (module, moduleControllers) ->
                """
                <div class="module">
                    <div class="module-header">
                        <h2>📦 $module 모듈</h2>
                        <div class="class-info">
                            ${moduleControllers.joinToString("<br>") { "📄 ${it.fileName} → ${it.className}" }}
                        </div>
                    </div>
                    <div class="endpoints">
                        ${moduleControllers.flatMap { it.endpoints }.joinToString("\n") { endpoint ->
                            """
                            <div class="endpoint">
                                <div class="endpoint-header">
                                    <span class="method ${endpoint.method.lowercase()}">${endpoint.method}</span>
                                    <span class="path">${endpoint.path}</span>
                                    <span class="function-name">${endpoint.functionName}</span>
                                </div>
                                <div class="details">
                                    ${if (endpoint.requestDetails.parameters.isNotEmpty()) {
                                        """
                                        <div class='detail-item'>
                                            <span class='detail-label'>📥 Request Parameters:</span>
                                            <div class='parameter-list'>
                                                ${endpoint.requestDetails.parameters.joinToString("<br>") { param ->
                                                    val annotation = if (param.annotation != null) "<span class='annotation'>${param.annotation}</span> " else ""
                                                    "<div class='parameter'><code>$annotation${param.type} ${param.name}</code></div>"
                                                }}
                                            </div>
                                        </div>
                                        """
                                    } else ""}
                                    ${if (endpoint.requestDetails.bodyType != null) "<div class='detail-item'><span class='detail-label'>📦 Request Body:</span> <code>${endpoint.requestDetails.bodyType}</code></div>" else ""}
                                    ${if (endpoint.requestDetails.pathVariables.isNotEmpty()) "<div class='detail-item'><span class='detail-label'>🔗 Path Variables:</span> <code>${endpoint.requestDetails.pathVariables.joinToString(", ")}</code></div>" else ""}
                                    ${if (endpoint.responseDetails.type.isNotEmpty()) {
                                        val responseInfo = if (endpoint.responseDetails.isList) {
                                            "List<${endpoint.responseDetails.genericType ?: "Unknown"}>"
                                        } else {
                                            endpoint.responseDetails.type
                                        }
                                        "<div class='detail-item'><span class='detail-label'>📤 Response:</span> <code>$responseInfo</code></div>"
                                    } else ""}
                                    ${if (endpoint.functionName.isNotEmpty()) "<div class='detail-item'><span class='detail-label'>🔧 Function:</span> <code>${endpoint.functionName}</code></div>" else ""}
                                </div>
                            </div>
                            """
                        }}
                    </div>
                </div>
                """
            }.joinToString("\n")}
            
            <div class="json-data">
                <strong>📄 JSON 데이터:</strong>
                $json
            </div>
        </div>
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
    val functionName: String,
    val requestType: String,
    val responseType: String,
    val requestDetails: RequestDetails,
    val responseDetails: ResponseDetails
)

data class RequestDetails(
    val parameters: List<ParameterInfo>,
    val bodyType: String?,
    val pathVariables: List<String>
)

data class ResponseDetails(
    val type: String,
    val isList: Boolean,
    val genericType: String?
)

data class ParameterInfo(
    val name: String,
    val type: String,
    val annotation: String?,
    val required: Boolean
)
