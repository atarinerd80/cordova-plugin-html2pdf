Html 2 Pdf
=============

This is a pdf creation plugin for Phonegap 3.3.0 / Cordova 3.3.1 supporting Android >=2.3.3 and iOS>=6.0.
It creates a pdf from the given html and stores it on the device.

There is one method:

* createhtml, filePath, pageSize, pageOrientation, successCallback, errorCallback

Installation
======
You may use phonegap CLI as follows:

<pre>
➜ phonegap local plugin add https://github.com/moderna/cordova-plugin-html2pdf.git
[phonegap] adding the plugin: https://github.com/moderna/cordova-plugin-html2pdf.git
[phonegap] successfully added the plugin
</pre>
  
It has been confirmed to work with cordova 3.3.0+

Usage
====
```javascript
document.addEventListener'deviceready', onDeviceReady;
function onDeviceReady
{
        var success = functionstatus {
            alert'Message: ' + status;
        }

        var error = functionstatus {
            alert'Error: ' + status;
        }

        window.html2pdf.create
            "<html><head></head><body><h1>Some</h1><p>html content.</p></body></html>",
            "~/Documents/test.pdf", // on iOS,
			// "test.pdf", on Android will be stored in /mnt/sdcard/at.modalog.cordova.plugin.html2pdf/test.pdf
			"Letter", //Full list of supported page sizes below
			"Landscape", //Choice between either 'Landscape'  or 'Portrait'
            success,
            error
        ;
}
```

Supported Page Sizes
====================
The following list displays all page sizes that can be used:

| PageName				| Dimensions mm 	  | Dimensions in 		|
| --------------------- | ------------------- | ------------------- |
| Letter 				|216 x 280 		  	  |8.5 x 11				|
| Note					|143 x 191			  |7.2 x 9.6			|
| Legal					|216 x 356		  	  |8.5 x 14				|
| Tabloid				|279 x 432		  	  |11 x 17				|
| Executive				|184 x 267		  	  |7.25 x 10.5			|	
| Postcard				|95 x 140			  |3.75 x 5.5			|
| A0					|841 x 1189		  	  |33.1 x 46.8			|
| A1					|594 x 841		  	  |23.4 x 33.1			|
| A2					|420 x 594		  	  |16.5 x 23.4			|
| A3					|297 x 420		  	  |11.7 x 16.5			|
| A4					|210 x 297		  	  |8.3 x 11.7			|
| A5					|148 x 210		  	  |5.8 x 8.3			|
| A6					|105 x 148		  	  |4.1 x 5.8			|
| A7					|74 x 105			  |2.9 x 4.1			|
| A8					|52 x 74			  |2 x 2.9				|
| A9					|37 x 52			  |1.5 x 2.0			|
| A10					|26 x 37			  |1 x 1.5				|
| B0					|1000 x 1414		  |39.4 x 55.7			|
| B1					|707 x 1000		  	  |27.8 x 39.4			|
| B2					|500 x 707		  	  |19.7 x 27.8			|
| B3					|353 x 500		  	  |13.9 x 19.7			|
| B4					|250 x 353		  	  |9.8 x 13.9			|
| B5					|176 x 250		  	  |6.9 x 9.8			|
| B6					|125 x 176		  	  |4.9 x 6.9			|
| B7					|88 x 125			  |3.5 x 4.9			|	
| B8					|62 x 88			  |2.4 x 3.5			|
| B9					|44 x 62			  |1.7 x 2.4			|
| B10					|61 x 44			  |1.2 x 1.7			|
| ArchE					|914 x 1219		  	  |36 x 48				|
| ArchD					|610 x 914		  	  |24 x 36				|
| ArchC					|457 x 610		  	  |18 x 24				|	
| ArchB					|305 x 457		  	  |12 x 18				|	
| ArchA					|229 x 305		  	  |9 x 12				|
| FLSA					|216 x 343		  	  |8.5 x 13.5			|
| FLSE					|210 x 330		  	  |8.25 x 13			|
| HalfLetter			|108 x 279		  	  |4.25 x 8.5			|
| Ledger				|279 x 432		  	  |11 x 17				|
| ID1					|86 x 54			  |3.3 x 2.1			|
| ID2					|105 x 74			  |4.1 x 2.9			|
| ID3					|125 x 88			  |4.9 x 3.5			|
| CrownQuarto			|189 x 246		  	  |7.4 x 9.7			|
| LargeCrownQuarto		|201 x 258		  	  |7.9 x 10.2			|
| DemyQuarto			|219 x 276		  	  |8.6 x 10.9			|
| RoyalQuarto			|237 x 312		  	  |9.3 x 12.25			|	
| CrownOctavo			|123 x 186		  	  |4.8 x 7.3			|
| LargeCrownOctavo		|129 x 198		  	  |5.1 x 7.8			|
| DemyOctavo			|138 x 216		  	  |5.4 x 8.5			|
| RoyalOctavo			|129 x 198		  	  |5.1 x 7.8			|
| SmallPaperback		|110 x 178		  	  |4.5 x 7				|
| PenguinSmallPaperback	|110 x 181		  	  |4.5 x 7				|
| PenguinLargePaperback	|129 x 198		  	  |5 x 7.75				|