# digdag-operator-appconfig

# 1) build

```sh
sbt compile
sbt publish
```

Artifacts are build on local repos: `./.digdag/plugins`.

# 2) run an example

```sh
digdag selfupdate
digdag run --project sample plugin.dig -a
```
