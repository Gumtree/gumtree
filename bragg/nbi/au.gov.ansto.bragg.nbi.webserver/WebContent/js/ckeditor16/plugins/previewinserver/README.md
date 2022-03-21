# Preview In Server for CKEditor 4
Feature to preview the text of CKEditor on a web page, via POST or GET

## Installation

* Download the plugin and extract the files in the **plugins** folder of CKEditor
* Enable the plugin by using the **extraPlugins** configuration setting

````js
config.extraPlugins = 'previewinserver';
````

## Configuration

URL Server
````js
config.previewInServerUrl = 'http://example.com/preview';
````

Method
````js
config.previewInServerMethod = 'Post';
````
or
````js
config.previewInServerMethod = 'Get';
````

## Field that will receive the text from CKEditor
Field name of your model on the server
````js
config.previewInServerNameFieldWithHtml = 'Text';
````

## Other fields of your model

Example:
````js
config.previewInServerFields = [
        {
            key: 'name1',
            value: 'value1',
            selector: 'selector1'
        },
        {
            key: 'name2',
            value: 'value2',
            selector: 'selector2'
        }
]
````
### Summary

| Field | Description |
|:---|:---|
|key|Name of field the your model|
|value|Value fixed|
|selector|Value obtained by the *document.querySelector* when sending the data to the server|

### Note
:warning: If you want a fixed value leave the **selector** empty, if you want a dynamic value leave the **value** empty, never fill in both fields<br/>
:warning: If necessary, enable pop-up in your browser

## References
This plugin was based [preview](https://ckeditor.com/cke4/addon/preview) and [ServerPreview](https://ckeditor.com/old/forums/CKEditor-3.x/CKEditor-and-serverpreview)
