def buildNode = 'master'
def appName = "exadel-frs-main-fe-${env.BRANCH_NAME}"
def containerName = "exadel-frs-main-fe"
def sonarProjectKey = "Exadel-frs-main-fe"
def sonarProjectName = 'Exadel-frs-main-fe'
def majorVersion = '0.1'
def appPort
def dockerCmd
def pipeline
def appDockerRunCommand
def fullVersion
def runCommand
def portDefinition

node(buildNode) {
	stage 'Cleanup'
	dir ('src'){ deleteDir() }
	dir ('ops'){ deleteDir() }
	stage 'Init'
	dir ('ops') {
		git changelog: false, poll: false, credentialsId: 'gitlabID', url: 'https://git.exadel.com/exadel-face-recognition-service/frs-devops'
		pipeline = load'Jenkins/Jenkinsfile_common.groovy'
	}
}
if ( env.ENV_NAME == "DEV" || env.ENV_NAME == "QA" || env.ENV_NAME == "PROD" ) {
	node(buildNode) {
		stage "Deploy to ${env.ENV_NAME}"
		dockerRegistryURL = pipeline.getDockerRegistryURL(appName)
		if ( env.ENV_NAME == 'DEV' ) {
			runCommand = ''
			portDefinition = '80:80 -p 443:443'
		} else if ( env.ENV_NAME == 'QA' ) {
			runCommand = ''
			portDefinition = '80:80 -p 443:443'
		} else if ( env.ENV_NAME == 'PROD' ) {
			runCommand = ''
			portDefinition = '80:80 -p 443:443'
		}
		appDockerRunCommand = "-p ${portDefinition} " +
                "-e SERVICE_NAME=${env.ENV_NAME}-${appName}-app " +
                "--name ${env.ENV_NAME}-${containerName}-app " +
                "${dockerRegistryURL}/app-${appName}:${DEPLOY_VERSION} " +
                "${runCommand}"
        pipeline.deployToEnv appName, containerName, DEPLOY_VERSION, appDockerRunCommand
	}
}
