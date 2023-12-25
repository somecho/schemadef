# Schemadef
Generate defaults from JSON schema. Supports
- `$defs`


## CLI Usage 
Here's the manpage:
```
NAME:
 schemadef - Generate defaults from JSON schema

USAGE:
  schemadef [command options] [arguments...]

VERSION:
 0.1.3

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

## Building from source
You will need to have the following dependencies on your system:
- Graal 21
- Clojure

Next, create the uberjar with `clojure -T:build uberjar`. Then create the native image with `clojure -T:build native-image`. The binary `schemadef` will be in the `/target` folder.
