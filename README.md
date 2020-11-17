# digdag-operator-appconfig

[![](https://jitpack.io/v/emanon-was/digdag-operator-aws-appconfig.svg)](https://jitpack.io/#emanon-was/digdag-operator-aws-appconfig) [![Digdag](https://img.shields.io/badge/digdag-v0.9.42-brightgreen.svg)](https://github.com/treasure-data/digdag/releases/tag/v0.9.42)

# Usage

```yaml

_export:
  plugin:
    repositories:
      - https://jitpack.io
    dependencies:
      - io.digdag.plugin:digdag-operator-aws-appconfig:1.0.0

+step1:
  aws.appconfig.get_configuration>:
  profile:
    region: "ap-northeast-1" # (required)
    # credentials: # (optional)
    #   access_key_id: "test1"
    #   secret_access_key: "test2"
  resource: # (required)
    application: "digdag" # (required)
    environment: "main" # (required)
    configuration: "json" # (required)
    client_id: "abcdefghijklmnopqrstuvwxyz" # (required)
    # client_configuration_version: "1" # (optional)
  store: "appconfig"

+step2:
  echo>: ${appconfig}

```

# Development

## 1) build

```sh
sbt compile
sbt publish
```

Artifacts are build on local repos: `./.digdag/plugins`.

## 2) run an example

```sh
digdag selfupdate
digdag run --project sample plugin.dig -a
```
