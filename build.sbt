lazy val `tcs-template-java-assembly` = project
  .settings(
    libraryDependencies ++= Dependencies.TcstemplatejavaAssembly
  )

lazy val `tcs-template-java-hcd` = project
  .settings(
    libraryDependencies ++= Dependencies.TcstemplatejavaHcd
  )

lazy val `tcs-template-java-client` = project
  .settings(
    libraryDependencies ++= Dependencies.TcstemplatejavaClient
  )

lazy val `tcs-template-java-deploy` = project
  .dependsOn(
    `tcs-template-java-assembly`,
    `tcs-template-java-hcd`,
      `tcs-template-java-client`
  )
  .enablePlugins(JavaAppPackaging, CswBuildInfo)
  .settings(
    libraryDependencies ++= Dependencies.TcstemplatejavaDeploy
  )
