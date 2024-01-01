VERSION=$(curl https://raw.githubusercontent.com/somecho/schemadef/main/resources/VERSION)
wget https://github.com/somecho/schemadef/releases/download/$VERSION/schemadef-$VERSION-linux-amd64.tar.gz 
tar -xvzf schemadef*.tar.gz
mv schemadef /usr/local/bin
rm schemadef*.tar.gz
if [ -f /usr/local/bin/schemadef ]; 
then
    echo "Success: Schemadef $VERSION has bin installed at /usr/local/bin"
fi
