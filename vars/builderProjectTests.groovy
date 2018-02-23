def retrieveArtifacts(stackname, testArtifacts) {
    def localTestArtifacts = []
    echo "Looking for test artifacts: ${testArtifacts}"
    for (int i = 0; i < testArtifacts.size(); i++) {
        def remoteTestArtifact = testArtifacts.get(i)
        def slash = remoteTestArtifact.lastIndexOf('/')
        def basename = remoteTestArtifact[slash+1..-1]
        def localTestArtifact = "${env.BUILD_TAG}.${basename}"
        localTestArtifacts << localTestArtifact
        builderTestArtifact localTestArtifact, stackname, remoteTestArtifact
    }
    echo "Retrieved test artifacts: ${localTestArtifacts}"
}

def defineProjectTests(stackname, folder) {
    def actions = [:]
    def projectTestsParallelScripts = findFiles(glob: 'project_tests/*')
    for (int i = 0; i < projectTestsParallelScripts.size(); i++) {
        def projectTestsParallelScript = "cd ${folder}; ${projectTestsParallelScripts[i].path}"
        actions[projectTestsParallelScripts[i].name] = {
            builderCmd stackname, projectTestsParallelScript
        }
    }
    def projectTestsCmd = "cd ${folder}; ./project_tests.sh"
    actions['project_tests.sh'] = {
        builderCmd stackname, projectTestsCmd
    }
    return actions
}

def call(stackname, folder, testArtifacts=[], order=['project', 'smoke']) {
    for (int i = 0; i < order.size(); i++) {
        if (order.get(i) == 'smoke') {
            builderSmokeTests stackname, folder
        } else if (order.get(i) == 'project') {
            try {
                actions = defineProjectTests stackname, folder
                parallel actions
            } finally {
                retrieveArtifacts stackname, testArtifacts
            }
        } else {
            error("You requested to run '${order.get(i)}' tests, but the only allowed values are 'smoke' and 'project'.");
        }
    }
}
