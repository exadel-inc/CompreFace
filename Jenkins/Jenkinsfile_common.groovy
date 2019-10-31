//placeholder
def getGitFullVersion(majorVersion) {
    sh "git rev-parse HEAD | cut -c 1-8 | tr -d '\n' > GIT_COMMIT"
    gitShortCommit = readFile('GIT_COMMIT')
    fullVersion = "${majorVersion}.${env.BUILD_NUMBER}-${gitShortCommit}"
    println "Version string: ${fullVersion}"
    return fullVersion
}

def getGitFullVersion(majorVersion, branchName) {
    updateGitlabCommitStatus state: 'pending'
    properties([gitLabConnection('gitlab')])
    gitLabConnection('gitlab')
    if ( branchName == "dev" ) {gitShortCommit='-rc'}
    else if ( branchName == "master" ) {gitShortCommit=''}
    else {gitShortCommit='-devel'}
    fullVersion = "${majorVersion}.${env.BUILD_NUMBER}${gitShortCommit}"
    println "Version string: ${fullVersion}"
    return fullVersion
}

def getDockerCmd(dockerType,appName) {
    def dockerCmd
    dockerCmd = 'sudo docker'
    return dockerCmd
}

def MarkInfo(msg) {
	ansiColor('xterm') {
		echo "[1;32m ${msg} [0m"
	}
}

def getDockerCmd(dockerType) {
    def dockerCmd
    dockerCmd = 'sudo docker'
    return dockerCmd
}

def getDockerRegistryURL(appName) {
    def dockerRegistryURL
    dockerRegistryURL='msqv502.exadel.by:5000'
    return dockerRegistryURL
}

def buildAndTest(appName, buildCmd, dockerType, versionString) {
    try {
        dockerCmd = getDockerCmd(dockerType,appName)
        if ( env.BRANCH_NAME == '' || env.BRANCH_NAME == null ) {env.BRANCH_NAME='temp'}
        def branchName = env.BRANCH_NAME
        def branchNameUpdated = branchName.replaceAll('/','_').toLowerCase()
        def appNameUpdated = appName.replaceAll("[^a-zA-Z0-9 ]+","_").toLowerCase()
        println "branchName: ${branchName}"
        println "branchNameUpdated: ${branchNameUpdated}"
        println "appName: ${appName}"
        println "appNameUpdated: ${appNameUpdated}"
        dockerRegistryURL = "vinv083.vn.exadel.com:5000"
        dir ('src') {
            sh "${dockerCmd} run -t --rm -e APP_VERSION_STRING='${versionString}' --name app-${appNameUpdated}_JENKINS-DEV-${env.BUILD_NUMBER} ${buildCmd}"
        }
    } catch (any) {
        sh "echo 'Stage BuildAndTest was failed. Exiting...' ; exit 1"
    }
}

def artifactArchive(fullVersion) {
    sh "zip -q -r app-${fullVersion}.zip . -x '*.git*' -x 'node_modules*'"
    archiveArtifacts "app-${fullVersion}.zip"
    stash includes: "app-${fullVersion}.zip", name: "app-${fullVersion}.zip"
    sh "rm -fv app-${fullVersion}.zip"
}

def assembleDockerImage(appName, dockerfileDir, fullVersion) {
    dockerCmd = 'docker'
    dockerRegistryURL = "msqv502.exadel.by:5000"
    dir ('ops'){
#        dir ("Docker/${dockerfileDir}") {
        dir ("../") {
            unstash "app-${fullVersion}.zip"
            sh "mv app-${fullVersion}.zip app.zip"
        }
#        sh "${dockerCmd} build --pull --build-arg=APP_VERSION_STRING='${fullVersion}' -t app-${appName}:JENKINS-${env.BUILD_NUMBER} Docker/${dockerfileDir}/"
        sh "${dockerCmd} build --pull --build-arg=APP_VERSION_STRING='${fullVersion}' -t app-${appName}:JENKINS-${env.BUILD_NUMBER} ../"
    }
    sh "${dockerCmd} rmi msqv502.exadel.by:5000/app-${appName}:latest || echo 'Not tagged yet'"
    sh "${dockerCmd} tag app-${appName}:JENKINS-${env.BUILD_NUMBER} ${dockerRegistryURL}/app-${appName}:latest"
    sh "${dockerCmd} tag app-${appName}:JENKINS-${env.BUILD_NUMBER} ${dockerRegistryURL}/app-${appName}:${fullVersion}"
}

def pushDockerImage(appName, fullVersion) {
    dockerCmd = 'docker'
    dockerRegistryURL = "msqv502.exadel.by:5000"
    sh "${dockerCmd} push ${dockerRegistryURL}/app-${appName}:latest"
    sh "${dockerCmd} push ${dockerRegistryURL}/app-${appName}:${fullVersion}"
}

def removehDockerImage(appName, fullVersion) {
    dockerCmd = 'docker'
    dockerRegistryURL = "msqv502.exadel.by:5000"
    sh "${dockerCmd} rmi ${dockerRegistryURL}/app-${appName}:latest || echo 'Not tagged..'"
    sh "${dockerCmd} rmi ${dockerRegistryURL}/app-${appName}:${fullVersion} || echo 'Not tagged..'"
    sh "${dockerCmd} rmi app-${appName}:JENKINS-${env.BUILD_NUMBER} || echo 'Not tagged..'"
}

def removeDockerImage(appName, fullVersion) {
    dockerCmd = 'docker'
    dockerRegistryURL = "msqv502.exadel.by:5000"
    sh "${dockerCmd} rmi ${dockerRegistryURL}/app-${appName}:latest || echo 'Not tagged..'"
    sh "${dockerCmd} rmi ${dockerRegistryURL}/app-${appName}:${fullVersion} || echo 'Not tagged..'"
    sh "${dockerCmd} rmi app-${appName}:JENKINS-${env.BUILD_NUMBER} || echo 'Not tagged..'"
}


def getDockerHosts(appName) {
    def dockerHosts
    if ( env.ENV_NAME == "DEV")      { dockerHosts = 'msqv500.exadel.by' }
    else if ( env.ENV_NAME == "QA")  { dockerHosts = 'msqv501.exadel.by' }
    else if ( env.ENV_NAME == "PROD")  { dockerHosts = ' msqvqwekjhfvs.exadel.by' }

    return dockerHosts
}

def deployToEnv(appName, containerName, deployVersion, appDockerRunCommand) {
    def deployVersionReal
    dockerHosts = getDockerHosts(appName)
    dockerCmd = getDockerCmd(appName)
    dockerRegistryURL = getDockerRegistryURL(appName)
    println "Docker host: ${dockerHosts}"
    println "Deploying ${appName}:${deployVersion} to ${env.ENV_NAME} environment..."
    sh """
        ${dockerCmd} -H ${dockerHosts} stop ${env.ENV_NAME}-${containerName}-app || echo 'No such container'
        ${dockerCmd} -H ${dockerHosts} rm ${env.ENV_NAME}-${containerName}-app || echo 'No such container'
        ${dockerCmd} -H ${dockerHosts} pull ${dockerRegistryURL}/app-${appName}:${deployVersion}
        ${dockerCmd} -H ${dockerHosts} run -d --restart='always' --label container_name=${env.ENV_NAME}-${appName}-app ${appDockerRunCommand}
    """
    return deployVersionReal
}

return this
