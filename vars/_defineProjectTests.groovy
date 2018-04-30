def call(stackname, folder, callable) {
    def String commit = elifeGitRevision()
    def actions = [:]
    def projectTestsParallelScripts = findFiles(glob: '.ci/*')
    for (int i = 0; i < projectTestsParallelScripts.size(); i++) {
        def projectTestsParallelScript = "cd ${folder}; ${projectTestsParallelScripts[i].path}"
        def name = "${projectTestsParallelScripts[i].name}"
        actions[name] = {
            withCommitStatus({
                callable stackname, projectTestsParallelScript
            }, name, commit)
        }
    }
    if (fileExists('project_tests.sh')) {
        def projectTestsCmd = "cd ${folder}; ./project_tests.sh"
        actions['project_tests.sh'] = {
            withCommitStatus({
                callable stackname, projectTestsCmd
            }, 'project_tests', commit)
        }
    }

    if (!actions) {
        throw new Exception("No .ci/ or project_tests.sh script was found")
    }

    return actions
}