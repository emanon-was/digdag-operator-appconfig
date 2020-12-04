# digdag-operator-appconfig

[![](https://jitpack.io/v/emanon-was/digdag-operator-aws-appconfig.svg)](https://jitpack.io/#emanon-was/digdag-operator-aws-appconfig) [![Digdag](https://img.shields.io/badge/digdag-v0.9.42-brightgreen.svg)](https://github.com/treasure-data/digdag/releases/tag/v0.9.42)

# Usage

```yaml

_export:
  plugin:
    repositories:
      - https://jitpack.io
    dependencies:
      - com.github.emanon-was:digdag-operator-aws-appconfig:1.1.0
  aws.configure:
    # credentials: # (optional)
    #   access_key_id: AKIAIOSFODNN7EXAMPLE # (required)
    #   secret_access_key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY # (required)
    # profile: # (optional)
    #   name: default # (optional)
    #   file: $HOME/.aws/credentials # (optional)
    # region: us-west-2 # (optional)
  aws.appconfig:
    # credentials: # (optional)
    #   access_key_id: AKIAIOSFODNN7EXAMPLE # (required)
    #   secret_access_key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY # (required)
    # profile: # (optional)
    #   name: default # (optional)
    #   file: $HOME/.aws/credentials # (optional)
    # region: us-west-2 # (optional)
  aws.appconfig.get_configuration:
    # credentials: # (optional)
    #   access_key_id: AKIAIOSFODNN7EXAMPLE # (required)
    #   secret_access_key: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY # (required)
    # profile: # (optional)
    #   name: default # (optional)
    #   file: $HOME/.aws/credentials # (optional)
    # region: us-west-2 # (optional)
    # params: # (required)
    #   application: sample # (required)
    #   environment: develop # (required)
    #   configuration: template # (required)
    #   client_id: abcdefghijklmnopqrstuvwxyz # (required)
    #   client_configuration_version: 1 # (optional)
    # output: _ # (optional)

+step1:
  aws.appconfig.get_configuration>:
  # credentials: # (optional)
  #   access_key_id: "AKIAIOSFODNN7EXAMPLE" # (required)
  #   secret_access_key: "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY" # (required)
  # profile: # (optional)
  #   name: default # (optional)
  #   file: $HOME/.aws/credentials # (optional)
  # region: "ap-northeast-1" # (optional)
  params:
    application: digdag
    environment: main
    configuration: json
    client_id: abcdefghijklmnopqrstuvwxyz
  output: _

+step2:
  echo>: ${_}

```

# Options

## 1) Params (params, output)

- params: (required)
  - application: string (required)
  - environment: string (required)
  - configuration: string (required)
  - client_id: string (required)
  - client_configuration_version: integer (optional)

- output: string(optional)

```yaml

_export:
  plugin:
    repositories:
      - https://jitpack.io
    dependencies:
      - com.github.emanon-was:digdag-operator-aws-appconfig:x.y.z

  aws.appconfig.get_configuration: # priority-2
    params: ...
    output: ...

+step1:
  aws.appconfig.get_configuration>: # priority-1
  params: ...
  output: ...

```

## 2) Resource (region)

- region: string (optional)

```yaml

_export:
  plugin:
    repositories:
      - https://jitpack.io
    dependencies:
      - com.github.emanon-was:digdag-operator-aws-appconfig:x.y.z

  aws.configure:
    region: ... # priority-4

  aws.appconfig:
    region: ... # priority-3

  aws.appconfig.get_configuration:
    region: ... # priority-2

+step1:
  aws.appconfig.get_configuration>:
  region: ... # priority-1

```

## 3) Authenticate (credentials, profile)

- credentials: (optional)
  - access_key_id: string (required)
  - secret_access_key: string (required)

- profile: (optional)
  - name: string (optional)
  - file: string (optional)

```yaml

_export:
  plugin:
    repositories:
      - https://jitpack.io
    dependencies:
      - com.github.emanon-was:digdag-operator-aws-appconfig:x.y.z

  aws.configure:
    credentials: # priority-4
      ....
    profile: # priority-8
      .....

  aws.appconfig:
    credentials: # priority-3
      ....
    profile: # priority-7
      .....

  aws.appconfig.get_configuration:
    credentials: # priority-2
      ....
    profile: # priority-6
      .....

+step1:
  aws.appconfig.get_configuration>:
  credentials: # priority-1
    ....
  profile: # priority-5
    .....

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
