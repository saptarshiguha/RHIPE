VER=0.44
FILES=build.xml conf ec2 java rhipe rhipe.jar 
# all: code dist

.PHONY : doc code 

all: code doc web

web: 
	cp index index.org
	sed -i "" 's/_VER_/${VER}/g' index.org 
	/Applications/Aquamacs\ Emacs.app/Contents/MacOS/Aquamacs\ Emacs  -l make.el
	mv index.html dist/
	rm index.org
	cp a.css dist/
doc: 
	rm -rf dist/doc/html
	rm -rf docbuild
	mkdir docbuild
	cp -r doc/* docbuild/
	sed  's/_VER_/${VER}/' doc/conf.py > docbuild/conf.py
	make  -f Makefile.doc html latex
	rm -rf docbuild
	mkdir -p dist
	cd build/latex/ && make all-pdf
	mv build/html dist/doc
	cp build/latex/rhipe.pdf dist/doc/rhipe.doc.pdf
	rm -rf build

code: 	
	rm -rf dist/dn
	make --directory code	VER=${VER}
	rm -rf code/build
	mkdir -p dist/dn
	mkdir dist/dn/rhipe.${VER}
	for x in ${FILES}; do     cp -r code/$$x dist/dn/rhipe.${VER}; done
	echo 'VER=${VER}' > dist/dn/rhipe.${VER}/Makefile
	cat code/Makefile >> dist/dn/rhipe.${VER}/Makefile
	cd dist/dn && tar cfz rhipe.${VER}.tgz rhipe.${VER}
	rm -rf dist/dn/rhipe.${VER}
	rm -rf dist/dn/rhipe.tgz
	cp dist/dn/rhipe.${VER}.tgz dist/dn/rhipe.tgz

clean:
	rm -rf dist


