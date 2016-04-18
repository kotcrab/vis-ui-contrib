#### ui-contrib

When contributing to `ui-contrib` lib follow main repository [CONTRIBUTING.md](https://github.com/kotcrab/vis-editor/blob/master/CONTRIBUTING.md) file.

#### skins

To contribute custom skin you should do the following:

1. Create separate directory for you skin.
2. Inside newly created directory follow this structure:
```
\- skins
 \- my-skin
  |- README.md
  |- x1-raw
  |- x2-raw (if your skin targets VisUI X2 skin scale, optional)
  |- x1 (packaged skin - see below)
  |- x2 (packaged skin - see below)
  |- my-skin.usl (or JSON, not required if you are simply re-skinning original VisUI skin)
  |- my-skin.svg (try to keep file size reasonable, optional)
```

In the `README.md` you should include basic information about your skin, which VisUI version it supports, which widgets are unsupported, etc. You should also specify on what
license you allow to use your skin. (highly recommend to use Apache2 - same license as VisUI)

`x1-raw`/`x1-raw` contains unpacked skin images. Skin packaging is automated by Gradle. From `skins` directory you can execute those tasks:

- `gradle pack` - packages png files in `x1-raw` and `x2-raw` into `x1` and `x2` directories
- `gradle skin` - If skin uses USL then compiles USL to JSON and places result in `x1` and `x2`. If skin uses JSON then it is copied to `x1` and `x2`.
- `gradle compile` - executes both `pack` and `skin` tasks
