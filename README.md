
# Rapid

A plain simple [Burp Suite](https://portswigger.net/burp) extension that enables you to save *HTTP Request & Response* data to a single file in one go.

## How it works?
Currently, **Rapid** offers two options:

 - Rapid - Save HTTP Request & Response
 - Rapid - Save Files & Screenshot

#### Option #1 - Save HTTP Request & Response
Writes the *HTTP Request & Response* data to a single destination file.

##### For example, right-click on an element from the Burp Suite's Proxy -> HTTP History tab, select 'Rapid - Save HTTP Request & Response', select a destination folder and enter a filename, e.g:

    get-homepage
   
   Click ***Save***, and **Rapid** will automatically create the following file on the destination folder:
```sh
get-homepage-[HTTP-Request-Response].txt
```
#### Option #2 - Save Files & Screenshot
Writes the *HTTP Request & Response* data to a single destination file but also captures a screenshot\*.

For example, given the filename ``` get-homepage ```, **Rapid** will generate the following files on your destination folder:
```sh
get-homepage-[HTTP-Request-Response].txt
get-homepage-[IMG].png
```
###
######  \* **Rapid** has been configured with a 1 second delay to capture a screenshot of whatever is shown on the screen the moment you click *Save*.

## Installation
1. Download the [latest](https://github.com/iamaldi/rapid/releases/latest) version of **Rapid**
2. Open Burp Suite, click on ```Extender```, then select the ```Extensions``` tab
3. Click ```Add```
4. Set ```'Extension type'``` to ```Java``` and click ```'Select file'```
5. Select the downloaded JAR file, ```rapid-vx.x-beta.jar``` and click ```'Open'``` 
6. Click ```'Next'``` and then ```'Close'```

## Issues & Feedback

Any feedback is welcome. If you have found a bug, have got errors while running **Rapid** or want to suggest a new feature, please create an new [issue](https://github.com/iamaldi/rapid/issues).

## License
This project is licensed under [GNU GPL v3.0](https://github.com/iamaldi/rapid/blob/master/LICENSE)
