# Schemadef
[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.some/schemadef.svg)](https://clojars.org/org.clojars.some/schemadef)

Simple CLI to generate defaults from JSON schema. Supports `$defs`. _Does not_ support
recursive `$refs` and JSON pointer URIs. Can also be used as a Clojure/script library.

## Installation
Schemadef currently only has prebuilt binaries for Linux x86-64 platforms. However, building from source is relatively trivial. See [building from source](#building-from-source).
To install, simply copy and paste this into your command line. You will need `sudo` because the script will copy the binary to `/usr/local/bin`. The script does not change or add any environment variables, neither does the binary create configuration files.
```sh
sudo bash < <(curl -s https://raw.githubusercontent.com/somecho/schemadef/main/install.sh)
```
To verify your installation, you can call `schemadef --help` and it should show you the man page. To uninstall, simply remove the binary with `sudo rm /usr/local/bin/schemadef`. 

### Building from source
You will need to have the following dependencies on your system:
- [Graal 21](https://www.graalvm.org/latest/docs/getting-started/)
- [Clojure](https://clojure.org/guides/install_clojure)

Next, create the uberjar with `clojure -T:build uberjar`. Then create the native image with `clojure -T:build native-image`. The binary `schemadef` will be in the `/target` folder.

## CLI Usage 
Here's the manpage:
```
NAME:
 schemadef - Generate defaults from JSON schema

USAGE:
  schemadef [command options] [arguments...]

VERSION:
 0.2.26

OPTIONS:
   -i, --input f   Path to JSON schema
   -s, --schema S  JSON schema literal
   -o, --output S  Output path. If not specified, prints to STDOUT.
   -?, --help
```

### Example 
Let's say we have the following schema saved as `schema.json`.

```json
{
    "$schema": "http://json-schema.org/draft-07/schema#",
    "title": "example",
    "type": "object",
    "properties": {
        "name": {
            "type": "string",
            "default": "adam"
        },
        "detail": {
            "type": "object",
            "properties": {
                "age": {
                    "$ref": "#/$defs/age"
                },
                "dim": {
                    "$ref": "#/$defs/dim"
                }
            }
        }
    },
    "$defs": {
        "age": {
            "type": "number",
            "default": 1
        },
        "height": {
            "type": "number",
            "default": 179
        },
        "dim": {
            "type": "object",
            "properties": {
                "height": {
                    "$ref": "#/$defs/height"
                }
            }
        }
    }
}
```

When `schemadef -i schema.json` is run, this gets printed to STDOUT: 
```json
{"name":"adam","detail":{"age":1,"dim":{"height":179}}}
```

You can use `schemadef -i schema.json -o default.json` to have the output be saved in a file.

## Use as a lib

Schemadef can also be used as a library. 

### deps.edn
```
org.clojars.some/schemadef {:mvn/version "0.2.26"}
```
### Leiningen
```
[org.clojars.some/schemadef "0.2.26"]
```

### Example
```clj
(require '[schemadef.lib :refer [gen-default]])
(gen-default (slurp "schema.json"))
```
