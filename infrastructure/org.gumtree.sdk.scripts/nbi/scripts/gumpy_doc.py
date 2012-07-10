from java.lang import System
from sphinx import cmdline
import os

sourceDir = System.getProperty('sphinx.sourceDir')
buildDir = System.getProperty('sphinx.buildDir')

# HTML
cmdline.main(['sphinx-build', '-b', 'html', sourceDir, buildDir + '/html'])

# Latex
cmdline.main(['sphinx-build', '-b', 'latex', sourceDir, buildDir + '/latex'])

# PDF
os.system('make -C ' + buildDir + '/latex all-pdf')
