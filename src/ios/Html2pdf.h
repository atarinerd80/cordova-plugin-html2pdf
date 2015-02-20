/*
 * Copyright (C) 2014
 * Html 2 Pdf iOS Code: Clément Wehrung <cwehrung@nurves.com> (https://github.com/iclems/iOS-htmltopdf)
 * Cordova 3.3 Plugin & Html 2 Pdf Android Code: Modern Alchemists OG <office@modalog.at> (http://modalog.at)
 */

#import <Foundation/Foundation.h>

#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVJSON.h>

#import "AppDelegate.h"

//Portrait Page Size Definitions
#define kPaperSizeLetterPortrait CGSizeMake(612.0, 792.0)
#define kPaperSizeNotePortrait CGSizeMake(540.0, 720.0)
#define kPaperSizeLegalPortrait CGSizeMake(612.0, 1008.0)
#define kPaperSizeTabloidPortrait CGSizeMake(792.0, 1224.0)
#define kPaperSizeExecutivePortrait CGSizeMake(522.0, 756.0)
#define kPaperSizePostcardPortrait CGSizeMake(283.0, 416.0)
#define kPaperSizeA0Portrait CGSizeMake(2384.0, 3370.0)
#define kPaperSizeA1Portrait CGSizeMake(1684.0, 2384.0)
#define kPaperSizeA2Portrait CGSizeMake(1191.0, 1684.0)
#define kPaperSizeA3Portrait CGSizeMake(842.0, 1191.0)  
#define kPaperSizeA4Portrait CGSizeMake(595.0, 842.0)
#define kPaperSizeA5Portrait CGSizeMake(420.0, 595.0)
#define kPaperSizeA6Portrait CGSizeMake(297.0, 420.0)
#define kPaperSizeA7Portrait CGSizeMake(210.0, 297.0)
#define kPaperSizeA8Portrait CGSizeMake(148.0, 210.0)
#define kPaperSizeA9Portrait CGSizeMake(105.0, 148.0)
#define kPaperSizeA10Portrait CGSizeMake(73.0, 105.0)
#define kPaperSizeB0Portrait CGSizeMake(2834.0, 4008.0)
#define kPaperSizeB1Portrait CGSizeMake(2004.0, 2834.0)
#define kPaperSizeB2Portrait CGSizeMake(1417.0, 2004.0)
#define kPaperSizeB3Portrait CGSizeMake(1000.0, 1417.0)
#define kPaperSizeB4Portrait CGSizeMake(708.0, 1000.0)
#define kPaperSizeB5Portrait CGSizeMake(498.0, 708.0)
#define kPaperSizeB6Portrait CGSizeMake(354.0, 498.0)
#define kPaperSizeB7Portrait CGSizeMake(249.0, 354.0)
#define kPaperSizeB8Portrait CGSizeMake(175.0, 249.0)
#define kPaperSizeB9Portrait CGSizeMake(124.0, 175.0)
#define kPaperSizeB10Portrait CGSizeMake(87.0, 124.0)
#define kPaperSizeARCHEPortrait CGSizeMake(2592.0, 3456.0)
#define kPaperSizeARCHDPortrait CGSizeMake(1728.0, 2592.0)
#define kPaperSizeARCHCPortrait CGSizeMake(1296.0, 1728.0)
#define kPaperSizeARCHBPortrait CGSizeMake(864.0, 1296.0)
#define kPaperSizeARCHAPortrait CGSizeMake(648.0, 864.0)
#define kPaperSizeFLSAPortrait CGSizeMake(612.0, 936.0)
#define kPaperSizeFLSEPortrait CGSizeMake(648.0, 936.0)
#define kPaperSizeHalfLetterPortrait CGSizeMake(396.0, 612.0)
#define kPaperSizeLedgerPortrait CGSizeMake(792.0, 1224.0)
#define kPaperSizeID1Portrait CGSizeMake(642.64999, 153.0)
#define kPaperSizeID2Portrait CGSizeMake(297.0, 210.0)
#define kPaperSizeID3Portrait CGSizeMake(354.0, 249.0)
#define kPaperSizeCrownQuartoPortrait CGSizeMake(535.0, 697.0)
#define kPaperSizeLargeCrownQuartoPortrait CGSizeMake(569.0, 731.0)
#define kPaperSizeDemyQuartoPortrait CGSizeMake(620.0, 782.0)
#define kPaperSizeRoyalQuartoPortrait CGSizeMake(671.0, 884.0)
#define kPaperSizeCrownOctavoPortrait CGSizeMake(348.0, 527.0)
#define kPaperSizeLargeCrownOctavoPortrait CGSizeMake(365.0, 561.0)
#define kPaperSizeDemyOctavoPortrait CGSizeMake(391.0, 612.0)
#define kPaperSizeRoyalOctavoPortrait CGSizeMake(442.0, 663.0)
#define kPaperSizeSmallPaperbackPortrait CGSizeMake(314.0, 504.0)
#define kPaperSizePenguinSmallPaperbackPortrait CGSizeMake(314.0, 513.0)
#define kPaperSizePenguinLargePaperbackPortrait CGSizeMake(365.0, 561.0)

//Landscape Page Size Definitions
#define kPaperSizeLetterLandscape CGSizeMake(792.0, 612.0)
#define kPaperSizeNoteLandscape CGSizeMake(720.0, 540.0)
#define kPaperSizeLegalLandscape CGSizeMake(1008.0, 612.0)
#define kPaperSizeTabloidLandscape CGSizeMake(1224.0, 792.0)
#define kPaperSizeExecutiveLandscape CGSizeMake(756.0, 522.0)
#define kPaperSizePostcardLandscape CGSizeMake(416.0, 283.0)
#define kPaperSizeA0Landscape CGSizeMake(3370.0, 2384.0)
#define kPaperSizeA1Landscape CGSizeMake(2384.0, 1684.0)
#define kPaperSizeA2Landscape CGSizeMake(1684.0, 1191.0)
#define kPaperSizeA3Landscape CGSizeMake(1191.0, 842.0)  
#define kPaperSizeA4Landscape CGSizeMake(842.0, 595.0)
#define kPaperSizeA5Landscape CGSizeMake(595.0, 420.0)
#define kPaperSizeA6Landscape CGSizeMake(420.0, 297.0)
#define kPaperSizeA7Landscape CGSizeMake(297.0, 210.0)
#define kPaperSizeA8Landscape CGSizeMake(210.0, 148.0)
#define kPaperSizeA9Landscape CGSizeMake(148.0, 105.0)
#define kPaperSizeA10Landscape CGSizeMake(105.0, 73.0)
#define kPaperSizeB0Landscape CGSizeMake(4008.0, 2834.0)
#define kPaperSizeB1Landscape CGSizeMake(2834.0, 2004.0)
#define kPaperSizeB2Landscape CGSizeMake(2004.0, 1417.0)
#define kPaperSizeB3Landscape CGSizeMake(1417.0, 1000.0)
#define kPaperSizeB4Landscape CGSizeMake(1000.0, 708.0)
#define kPaperSizeB5Landscape CGSizeMake(708.0, 498.0)
#define kPaperSizeB6Landscape CGSizeMake(498.0, 354.0)
#define kPaperSizeB7Landscape CGSizeMake(354.0, 249.0)
#define kPaperSizeB8Landscape CGSizeMake(249.0, 175.0)
#define kPaperSizeB9Landscape CGSizeMake(175.0, 124.0)
#define kPaperSizeB10Landscape CGSizeMake(124.0, 87.0)
#define kPaperSizeARCHELandscape CGSizeMake(3456.0, 2592.0)
#define kPaperSizeARCHDLandscape CGSizeMake(2592.0, 1728.0)
#define kPaperSizeARCHCLandscape CGSizeMake(1728.0, 1296.0)
#define kPaperSizeARCHBLandscape CGSizeMake(1296.0, 864.0)
#define kPaperSizeARCHALandscape CGSizeMake(864.0, 648.0)
#define kPaperSizeFLSALandscape CGSizeMake(936.0, 612.0)
#define kPaperSizeFLSELandscape CGSizeMake(936.0, 648.0)
#define kPaperSizeHalfLetterLandscape CGSizeMake(612.0, 396.0)
#define kPaperSizeLedgerLandscape CGSizeMake(1224.0, 792.0)
#define kPaperSizeID1Landscape CGSizeMake(153.0, 642.64999)
#define kPaperSizeID2Landscape CGSizeMake(210.0, 297.0)
#define kPaperSizeID3Landscape CGSizeMake(249.0, 354.0)
#define kPaperSizeCrownQuartoLandscape CGSizeMake(697.0, 535.0)
#define kPaperSizeLargeCrownQuartoLandscape CGSizeMake(731.0, 569.0)
#define kPaperSizeDemyQuartoLandscape CGSizeMake(782.0, 620.0)
#define kPaperSizeRoyalQuartoLandscape CGSizeMake(884.0, 671.0)
#define kPaperSizeCrownOctavoLandscape CGSizeMake(527.0, 348.0)
#define kPaperSizeLargeCrownOctavoLandscape CGSizeMake(561.0, 365.0)
#define kPaperSizeDemyOctavoLandscape CGSizeMake(612.0, 391.0)
#define kPaperSizeRoyalOctavoLandscape CGSizeMake(663.0, 442.0)
#define kPaperSizeSmallPaperbackLandscape CGSizeMake(504.0, 314.0)
#define kPaperSizePenguinSmallPaperbackLandscape CGSizeMake(513.0, 314.0)
#define kPaperSizePenguinLargePaperbackLandscape CGSizeMake(561.0, 365.0)       

@interface Html2pdf : CDVPlugin <UIWebViewDelegate, UIDocumentInteractionControllerDelegate>
{
}

// read / write
-(void) create: (CDVInvokedUrlCommand*)command;

// retain command for async repsonses
@property (nonatomic, strong) CDVInvokedUrlCommand* command;
@property (nonatomic, strong) NSString* filePath;
@property (nonatomic, assign) CGSize pageSize;
@property (nonatomic, assign) UIEdgeInsets pageMargins;
@property (retain) UIDocumentInteractionController* documentController;

@end
