/*
 Copyright 2014 Modern Alchemists OG

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

/*
 * Html 2 Pdf iOS Code: Clément Wehrung <cwehrung@nurves.com> (https://github.com/iclems/iOS-htmltopdf)
 */

#import "Html2pdf.h"

@interface Html2pdf (Private)

- (BOOL) saveHtml:(NSString*)html asPdf:(NSString*)filePath;

@end

@interface UIPrintPageRenderer (PDF)

- (NSData*) printToPDF;

@end

@implementation Html2pdf

@synthesize command, filePath, pageSize, pageMargins, documentController;

- (void)create:(CDVInvokedUrlCommand*)command
{
    self.command = command;
    
    NSArray* arguments = command.arguments;

    NSLog(@"Creating pdf from html has been started.");
    
    NSString* html = [arguments objectAtIndex:0];
    self.filePath  = [[arguments objectAtIndex:1] stringByExpandingTildeInPath];
    
    // Set the base URL to be the www directory.
    NSString* wwwFilePath = [[NSBundle mainBundle] pathForResource:@"www" ofType:nil];
    NSURL*    baseURL     = [NSURL fileURLWithPath:wwwFilePath];
    
    // define page size and margins
	switch ([arguments objectAtIndex:3])
	{
		case "Portrait":
			switch ([arguments objectAtIndex:2])
			{
				case "Letter":
					self.pageSize = kPaperSizeLetterPortrait;
					break;
				case "Note":
					self.pageSize = kPaperSizeNotePortrait;
					break;
				case "Legal":
					self.pageSize = kPaperSizeLegalPortrait;
					break;
				case "Tabloid":
					self.pageSize = kPaperSizeTabloidPortrait;
					break;
				case "Executive":
					self.pageSize = kPaperSizeExecutivePortrait;
					break;
				case "Postcard":
					self.pageSize = kPaperSizePostcardPortrait;
					break;
				case "A0":
					self.pageSize = kPaperSizeA0Portrait;
					break;
				case "A1":
					self.pageSize = kPaperSizeA1Portrait;
					break;
				case "A2":
					self.pageSize = kPaperSizeA2Portrait;
					break;
				case "A3":
					self.pageSize = kPaperSizeA3Portrait;
					break;
				case "A4":
					self.pageSize = kPaperSizeA4Portrait;
					break;
				case "A5":
					self.pageSize = kPaperSizeA5Portrait;
					break;
				case "A6":
					self.pageSize = kPaperSizeA6Portrait;
					break;
				case "A7":
					self.pageSize = kPaperSizeA7Portrait;
					break;
				case "A8":
					self.pageSize = kPaperSizeA8Portrait;
					break;
				case "A9":
					self.pageSize = kPaperSizeA9Portrait;
					break;
				case "A10":
					self.pageSize = kPaperSizeA10Portrait;
					break;
				case "B0":
					self.pageSize = kPaperSizeB0Portrait;
					break;
				case "B1":
					self.pageSize = kPaperSizeB1Portrait;
					break;
				case "B2":
					self.pageSize = kPaperSizeB2Portrait;
					break;
				case "B3":
					self.pageSize = kPaperSizeB3Portrait;
					break;
				case "B4":
					self.pageSize = kPaperSizeB4Portrait;
					break;
				case "B5":
					self.pageSize = kPaperSizeB5Portrait;
					break;
				case "B6":
					self.pageSize = kPaperSizeB6Portrait;
					break;
				case "B7":
					self.pageSize = kPaperSizeB7Portrait;
					break;
				case "B8":
					self.pageSize = kPaperSizeB8Portrait;
					break;
				case "B9":
					self.pageSize = kPaperSizeB9Portrait;
					break;
				case "B10":
					self.pageSize = kPaperSizeB10Portrait;
					break;
				case "ArchE":
					self.pageSize = kPaperSizeARCHEPortrait;
					break;
				case "ArchD":
					self.pageSize = kPaperSizeARCHDPortrait;
					break;
				case "ArchC":
					self.pageSize = kPaperSizeARCHCPortrait;
					break;
				case "ArchB":
					self.pageSize = kPaperSizeARCHBPortrait;
					break;
				case "ArchA":
					self.pageSize = kPaperSizeARCHAPortrait;
					break;
				case "FLSA":
					self.pageSize = kPaperSizeFLSAPortrait;
					break;
				case "FLSE":
					self.pageSize = kPaperSizeFLSEPortrait;
					break;
				case "HalfLetter":
					self.pageSize = kPaperSizeHalfLetterPortrait;
					break;
				case "Ledger":
					self.pageSize = kPaperSizeLedgerPortrait;
					break;
				case "ID1":
					self.pageSize = kPaperSizeID1Portrait;
					break;
				case "ID2":
					self.pageSize = kPaperSizeID2Portrait;
					break;
				case "ID3":
					self.pageSize = kPaperSizeID3Portrait;
					break;
				case "CrownQuarto":
					self.pageSize = kPaperSizeCrownQuartoPortrait;
					break;
				case "LargeCrownQuarto":
					self.pageSize = kPaperSizeLargeCrownQuartoPortrait;
					break;
				case "DemyQuarto":
					self.pageSize = kPaperSizeDemyQuartoPortrait;
					break;
				case "RoyalQuarto":
					self.pageSize = kPaperSizeRoyalQuartoPortrait;
					break;
				case "CrownOctavo":
					self.pageSize = kPaperSizeCrownOctavoPortrait;
					break;
				case "LargeCrownOctavo":
					self.pageSize = kPaperSizeLargeCrownOctavoPortrait;
					break;
				case "DemyOctavo":
					self.pageSize = kPaperSizeDemyOctavoPortrait;
					break;
				case "RoyalOctavo":
					self.pageSize = kPaperSizeRoyalOctavoPortrait;
					break;
				case "SmallPaperback":
					self.pageSize = kPaperSizeSmallPaperbackPortrait;
					break;
				case "PenguinSmallPaperback":
					self.pageSize = kPaperSizePenguinSmallPaperbackPortrait;
					break;
				case "PenguinLargePaperback":
					self.pageSize = kPaperSizePenguinLargePaperbackPortrait;
					break;	
				default:
					self.pageSize = kPaperSizeLetterPortrait;
					break;
			}
			break;
		case "Landscape":
			switch ([arguments objectAtIndex:2])
			{
				case "Letter":
					self.pageSize = kPaperSizeLetterLandscape;
					break;
				case "Note":
					self.pageSize = kPaperSizeNoteLandscape;
					break;
				case "Legal":
					self.pageSize = kPaperSizeLegalLandscape;
					break;
				case "Tabloid":
					self.pageSize = kPaperSizeTabloidLandscape;
					break;
				case "Executive":
					self.pageSize = kPaperSizeExecutiveLandscape;
					break;
				case "Postcard":
					self.pageSize = kPaperSizePostcardLandscape;
					break;
				case "A0":
					self.pageSize = kPaperSizeA0Landscape;
					break;
				case "A1":
					self.pageSize = kPaperSizeA1Landscape;
					break;
				case "A2":
					self.pageSize = kPaperSizeA2Landscape;
					break;
				case "A3":
					self.pageSize = kPaperSizeA3Landscape;
					break;
				case "A4":
					self.pageSize = kPaperSizeA4Landscape;
					break;
				case "A5":
					self.pageSize = kPaperSizeA5Landscape;
					break;
				case "A6":
					self.pageSize = kPaperSizeA6Landscape;
					break;
				case "A7":
					self.pageSize = kPaperSizeA7Landscape;
					break;
				case "A8":
					self.pageSize = kPaperSizeA8Landscape;
					break;
				case "A9":
					self.pageSize = kPaperSizeA9Landscape;
					break;
				case "A10":
					self.pageSize = kPaperSizeA10Landscape;
					break;
				case "B0":
					self.pageSize = kPaperSizeB0Landscape;
					break;
				case "B1":
					self.pageSize = kPaperSizeB1Landscape;
					break;
				case "B2":
					self.pageSize = kPaperSizeB2Landscape;
					break;
				case "B3":
					self.pageSize = kPaperSizeB3Landscape;
					break;
				case "B4":
					self.pageSize = kPaperSizeB4Landscape;
					break;
				case "B5":
					self.pageSize = kPaperSizeB5Landscape;
					break;
				case "B6":
					self.pageSize = kPaperSizeB6Landscape;
					break;
				case "B7":
					self.pageSize = kPaperSizeB7Landscape;
					break;
				case "B8":
					self.pageSize = kPaperSizeB8Landscape;
					break;
				case "B9":
					self.pageSize = kPaperSizeB9Landscape;
					break;
				case "B10":
					self.pageSize = kPaperSizeB10Landscape;
					break;
				case "ArchE":
					self.pageSize = kPaperSizeARCHELandscape;
					break;
				case "ArchD":
					self.pageSize = kPaperSizeARCHDLandscape;
					break;
				case "ArchC":
					self.pageSize = kPaperSizeARCHCLandscape;
					break;
				case "ArchB":
					self.pageSize = kPaperSizeARCHBLandscape;
					break;
				case "ArchA":
					self.pageSize = kPaperSizeARCHALandscape;
					break;
				case "FLSA":
					self.pageSize = kPaperSizeFLSALandscape;
					break;
				case "FLSE":
					self.pageSize = kPaperSizeFLSELandscape;
					break;
				case "HalfLetter":
					self.pageSize = kPaperSizeHalfLetterLandscape;
					break;
				case "Ledger":
					self.pageSize = kPaperSizeLedgerLandscape;
					break;
				case "ID1":
					self.pageSize = kPaperSizeID1Landscape;
					break;
				case "ID2":
					self.pageSize = kPaperSizeID2Landscape;
					break;
				case "ID3":
					self.pageSize = kPaperSizeID3Landscape;
					break;
				case "CrownQuarto":
					self.pageSize = kPaperSizeCrownQuartoLandscape;
					break;
				case "LargeCrownQuarto":
					self.pageSize = kPaperSizeLargeCrownQuartoLandscape;
					break;
				case "DemyQuarto":
					self.pageSize = kPaperSizeDemyQuartoLandscape;
					break;
				case "RoyalQuarto":
					self.pageSize = kPaperSizeRoyalQuartoLandscape;
					break;
				case "CrownOctavo":
					self.pageSize = kPaperSizeCrownOctavoLandscape;
					break;
				case "LargeCrownOctavo":
					self.pageSize = kPaperSizeLargeCrownOctavoLandscape;
					break;
				case "DemyOctavo":
					self.pageSize = kPaperSizeDemyOctavoLandscape;
					break;
				case "RoyalOctavo":
					self.pageSize = kPaperSizeRoyalOctavoLandscape;
					break;
				case "SmallPaperback":
					self.pageSize = kPaperSizeSmallPaperbackLandscape;
					break;
				case "PenguinSmallPaperback":
					self.pageSize = kPaperSizePenguinSmallPaperbackLandscape;
					break;
				case "PenguinLargePaperback":
					self.pageSize = kPaperSizePenguinLargePaperbackLandscape;
					break;
				default:
					self.pageSize = kPaperSizeLetterLandscape;
					break;
			}
			break;
		default:
			self.pageSize = kPaperSizeLetterPortrait;
			break;
	}
    self.pageMargins = UIEdgeInsetsMake(10, 5, 10, 5);
    
    // Load page into a webview and use its formatter to print the page
    UIWebView* webPage    = [[UIWebView alloc] init];
    webPage.delegate = self;
    webPage.frame = CGRectMake(0, 0, 1, 1); // Make web view small ...
    webPage.alpha = 0.0;                    // ... and invisible.
    [self.webView.superview addSubview:webPage];
    
    [webPage loadHTMLString:html baseURL:baseURL];
}

- (void)success
{
    NSString* resultMsg = [NSString stringWithFormat:@"HTMLtoPDF did succeed (%@)", self.filePath];
    NSLog(@"%@",resultMsg);
    
    // create acordova result
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsString:[resultMsg stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    
    // send cordova result
    [self writeJavascript:[result toSuccessCallbackString:command.callbackId]];
}

- (void)error:(NSString*)message
{
    NSString* resultMsg = [NSString stringWithFormat:@"HTMLtoPDF did fail (%@)", message];
    NSLog(@"%@",resultMsg);
    
    // create cordova result
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                messageAsString:[resultMsg stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    
    // send cordova result
    [self writeJavascript:[result toErrorCallbackString:command.callbackId]];

}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
    NSLog(@"Html2Pdf webViewDidFinishLoad");
    
    UIPrintPageRenderer *render = [[UIPrintPageRenderer alloc] init];
    
    [render addPrintFormatter:webView.viewPrintFormatter startingAtPageAtIndex:0];
    
    CGRect printableRect = CGRectMake(self.pageMargins.left,
                                      self.pageMargins.top,
                                      self.pageSize.width - self.pageMargins.left - self.pageMargins.right,
                                      self.pageSize.height - self.pageMargins.top - self.pageMargins.bottom);
    
    CGRect paperRect = CGRectMake(0, 0, self.pageSize.width, self.pageSize.height);
    
    [render setValue:[NSValue valueWithCGRect:paperRect] forKey:@"paperRect"];
    [render setValue:[NSValue valueWithCGRect:printableRect] forKey:@"printableRect"];
    
    if (filePath) {
        [[render printToPDF] writeToFile: filePath atomically: YES];
    }
    

    // remove webPage
    [webView stopLoading];
    webView.delegate = nil;
    [webView removeFromSuperview];
    webView = nil;

    // trigger success response
    [self success];

    // show "open pdf with ..." menu
    NSURL* url = [NSURL fileURLWithPath:filePath];
    self.documentController = [UIDocumentInteractionController interactionControllerWithURL:url];

    documentController.delegate = self;

    UIView* view = self.webView.superview;
    CGRect rect = view.frame; // open in top center
    rect.size.height *= 0.02;

    BOOL isValid = [documentController presentOpenInMenuFromRect:rect inView:view animated:YES];
    
    if (!isValid) {
        NSString* messageString = [NSString stringWithFormat:@"No PDF reader was found on your device. Please download a PDF reader (eg. iBooks or Acrobat)."];
        
        UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:@"Error" message:messageString delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [alertView show];
        //[alertView release]; // p. leak
    }

}

- (void)webView:(UIWebView *)webView didFailLoadWithError:(NSError *)error
{
    NSLog(@"webViewDidFailLoadWithError");
    
    // trigger error response
    [self error:[error description]];
}


@end


@implementation UIPrintPageRenderer (PDF)

- (NSData*) printToPDF
{
    NSMutableData *pdfData = [NSMutableData data];
    
    UIGraphicsBeginPDFContextToData( pdfData, self.paperRect, nil );
    
    [self prepareForDrawingPages: NSMakeRange(0, self.numberOfPages)];
    
    CGRect bounds = UIGraphicsGetPDFContextBounds();
    
    for ( int i = 0 ; i < self.numberOfPages ; i++ )
    {
        UIGraphicsBeginPDFPage();
        
        [self drawPageAtIndex: i inRect: bounds];
    }
    
    UIGraphicsEndPDFContext();
    
    return pdfData;
}

@end