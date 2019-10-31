def buildNode = 'master'
def appName = "Exadel-frs-main-be-${env.BRANCH_NAME}"
def containerName = "Exadel-frs-main-be"
def sonarProjectKey = "Exadel-frs-main-be"
def sonarProjectName = 'Exadel-frs-main-be'
def majorVersion = '0.1'
def appPort
def dockerCmd
def pipeline
def appDockerRunCommand
def fullVersion
def runCommand

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
		if ( env.ENV_NAME == 'DEV') {
			portDefinition = '8080:8080'
			envName = 'dev'
		} else if ( env.ENV_NAME == 'QA') {
			portDefinition = '8080:8080'
			envName = 'qa'
		} else if ( env.ENV_NAME == 'PROD') {
			portDefinition = '8080:8080'
			envName = 'prod'
		}

		appDockerRunCommand = "-p ${portDefinition} " +
                "--name ${env.ENV_NAME}-${containerName}-app " +
                "${dockerRegistryURL}/app-${appName}:${DEPLOY_VERSION} "

        pipeline.deployToEnv appName, containerName, DEPLOY_VERSION, appDockerRunCommand
	}
}
