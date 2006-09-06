# -*-ruby-*-

mvvm = Package['mvvm']
gui = Package['gui']

## Implementation
deps = Jars::Base + Jars::Gui + Jars::TomcatEmb + [mvvm['api']]

jt = JarTarget.buildTarget(gui, deps, 'impl', 'gui/impl')
$InstallTarget.installJars(jt, gui.getWebappDir('webstart'), nil, true)

ServletBuilder.new(gui, 'com.metavize.gui.webstart.jsp',
                   'gui/servlets/webstart', [], [], [$BuildEnv.servletcommon],
                   false, ['gui.jnlp', 'gui-local.jnlp', 'index.jsp'])

$InstallTarget.installJars(Jars::Gui, gui.getWebappDir('webstart'), nil, true)

guiRuntimeJars = ['asm.jar', 'cglib-2.1.3.jar', 'commons-logging-1.0.4.jar',
                  'log4j-1.2.11.jar' ].map do |f|
  Jars.downloadTarget("hibernate-3.2/lib/#{f}")
end
guiRuntimeJars << Jars.downloadTarget('hibernate-client/hibernate-client.jar')
$InstallTarget.installJars(guiRuntimeJars, gui.getWebappDir('webstart'), nil, true)
