// TODO: reorder this horrible list of parameters
// better: http://docs.groovy-lang.org/latest/html/documentation/#_named_arguments
def call(Closure preliminaryStep=null, marker=null, environmentName='end2end', processes=15, revision='master', articleId=null) {
    lock(environmentName) {
        if (environmentName == 'end2end') {
            builderStartAll(elifeEnd2endStacks())
        }
        if (preliminaryStep != null) {
            preliminaryStep()
        }

        def additionalFilteringArguments = ''
        if (marker) {
            additionalFilteringArguments = additionalFilteringArguments + "-m ${marker} "
        }
        if (articleId) {
            additionalFilteringArguments = additionalFilteringArguments + "--article-id=${articleId} "
        }

        sh "cd ${env.SPECTRUM_PREFIX}; sudo -H -u elife ${env.SPECTRUM_PREFIX}checkout.sh ${revision}"
        if (!additionalFilteringArguments) {
            // before starting the whole suite, run simple smoke test first
            sh "cd ${env.SPECTRUM_PREFIX}; SPECTRUM_ENVIRONMENT=${environmentName} SPECTRUM_TIMEOUT=120 sudo -H -u elife ${env.SPECTRUM_PREFIX}execute-simplest-possible-test.sh"
        }
        sh "cd ${env.SPECTRUM_PREFIX}; SPECTRUM_ENVIRONMENT=${environmentName} SPECTRUM_PROCESSES=${processes} sudo -H -u elife ${env.SPECTRUM_PREFIX}execute.sh ${additionalFilteringArguments}|| echo TESTS FAILED"
        
        def testXmlArtifact = "${env.BUILD_TAG}.${environmentName}.junit.xml"
        sh "cp ${env.SPECTRUM_PREFIX}build/junit.xml ${testXmlArtifact}"
        builderTestArtifact testXmlArtifact

        def testLogArtifact = "${env.BUILD_TAG}.${environmentName}.log"
        sh "cp ${env.SPECTRUM_PREFIX}build/test.log ${testLogArtifact}"
        archive testLogArtifact

        elifeVerifyJunitXml testXmlArtifact
    }
}
