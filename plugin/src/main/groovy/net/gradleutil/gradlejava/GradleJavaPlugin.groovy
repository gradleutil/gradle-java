package net.gradleutil.gradlejava

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.wrapper.Wrapper

import javax.inject.Inject


class GradleJavaPlugin implements Plugin<Project> {
    static final String shFileName = 'gradle-java.sh'
    static final String batFileName = 'gradle-java.bat'
    GradleJavaExtension extension

    static abstract class GradleJavaExtension {

        abstract DirectoryProperty getTemporaryDir();

        abstract DirectoryProperty getScriptDir();

        abstract Property<String> getJavaDir();

        abstract Property<String> getType();

        abstract Property<String> getMajorVersion();

        abstract Property<String> getRelease();

        @Inject
        GradleJavaExtension(Project project) {
			type.convention( 'jdk' )
            temporaryDir.convention( project.layout.dir( project.provider { project.file( '/tmp') }))
            scriptDir.convention(project.layout.dir(project.provider { project.rootProject.file('gradle') }))
            majorVersion.convention('11')
            release.convention('latest')
            project.afterEvaluate{
                javaDir.convention( "\${HOME}/wrapper/java/${majorVersion.get()}/${type.get()}/${release.get()}")
            }
        }
    }

    void apply(Project project) {
        extension = project.getObjects().newInstance(GradleJavaExtension)
        project.extensions.add('gradleJava', extension)

        project.rootProject.tasks.named('wrapper', Wrapper) {
            //distributionType = Wrapper.DistributionType.ALL

            doFirst {
                extension.scriptDir.asFile.get().mkdirs()
                new File(extension.javaDir.get()).mkdirs()
                [shFileName, batFileName].each { String fileName ->
                    File output = new File(extension.scriptDir.getAsFile().get(), fileName.split('/').last())
                    if (!output.exists()) {
                        String resource = GradleJavaPlugin.class.getResourceAsStream('/' + fileName).text
                        output << resource
                        output.setExecutable(true)
                    }
                }
            }

            doLast {
                def javaRelease = System.getenv('JVM_RELEASE') ?: extension.release.get()
                def javaMajorVersion = System.getenv('JVM_MAJOR_VERSION') ?: extension.majorVersion.get()
                def javaDir = System.getenv('JVM_HOME') ?: extension.javaDir.get()
                def envVars = "\nexport JVM_MAJOR_VERSION=${javaMajorVersion}\nexport JVM_DIR=\"${javaDir}\"\nexport JVM_RELEASE=${javaRelease}\nexport GRADLE_USER_HOME=\"\${HOME}/.gradle\"\n"
                envVars += ". \"\${APP_HOME}/gradle/${shFileName}\"\n"
                scriptFile.text = new StringBuilder(scriptFile.text).insert(scriptFile.text.indexOf('APP_NAME') - 1, envVars)
            }

        }
    }

}
