#### ui-contrib

When contributing to `ui-contrib` lib follow main repository [CONTRIBUTING.md](https://github.com/kotcrab/vis-editor/blob/master/CONTRIBUTING.md) file.

#### skins

To contribute custom skin you should do the following:

1. Create separate directory for you skin.
2. Inside this directory follow this structure:
```
\- skins
 \- my-skin
  |- README.md
  |- x1 
  |- x2 (if your skin targets VisUI X2 skin scale, optional)
  |- my-skin.usl (or JSON, not required if you are simply re-skinning original VisUI skin)
  |- my-skin.svg (try to keep file size reasonable, optional)
```
`x1`/`x2` should contain unpacked skin images. 
In the README.md you should include basic information about your skin, which VisUI version it supports, which widgets are unsupported etc. 
