resolvers ++= Seq(
    "Sonatype snapshots"                                 at "http://oss.sonatype.org/content/repositories/snapshots/",
    "jgit-repo"                                          at "http://download.eclipse.org/jgit/maven",
    "Twitter Repo"                                       at "http://maven.twttr.com/",
    "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
    Resolver.bintrayRepo("websudos", "oss-releases")
)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.websudos" % "sbt-package-dist" % "1.2.0")

