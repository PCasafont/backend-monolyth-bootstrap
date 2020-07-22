import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.task
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.testing.jacoco.tasks.JacocoReport

fun Project.prettyVersion(): String {
    var version = rootProject.version.toString()
    if (version.contains("+")) {
        version = version.substring(0, version.length - 8).replace("+", ".")
        if (version.endsWith("develop")) {
            version = version.substring(0, version.length - 8)
        }
    }
    return version
}

val Project.sourceSets: SourceSetContainer
    get() = extensions.getByName("sourceSets") as SourceSetContainer

fun Project.setupJar(
    specTitle: String,
    packagePath: String
) {
    tasks.named<Jar>("jar") {
        archiveFileName.set("${project.name}-${prettyVersion()}.jar")
        manifest.attributes.apply {
            set("Name", packagePath.replace('.', '/'))
            set("Specification-Title", specTitle)
            set("Specification-Version", prettyVersion())
            set("Specification-Vendor", "ACME")
            set("Implementation-Title", packagePath)
            set("Implementation-Version", prettyVersion())
            set("Implementation-Vendor", "ACME")
        }
    }
}
