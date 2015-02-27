package at.modalog.cordova.plugin.html2pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSizes;
import com.lowagie.text.pdf.PdfWriter;

import pageOptions.PageSizess;
import pageOptions.pageOrientationss;

import android.R.bool;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.printservice.PrintJob;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;

@TargetApi(19)
public class Html2pdf extends CordovaPlugin
{
	private static final String LOG_TAG = "Html2Pdf";
	private CallbackContext callbackContext;
	
	// change your path on the sdcard here
	private String publicTmpDir = ".at.modalog.cordova.plugin.html2pdf"; // prepending a dot "." would make it hidden
	private String tmpPdfName = "print.pdf";	
	
	// set to true to see the webview (useful for debugging)
    private final boolean showWebViewForDebugging = false;

	/**
	 * Constructor.
	 */
	public Html2pdf() {

	}

    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException
    {
		try
		{
			if( action.equals("create") )
			{
				if( showWebViewForDebugging )
				{
					Log.v(LOG_TAG,"java create pdf from html called");
					Log.v(LOG_TAG, "File: " + args.getString(1));
					// Log.v(LOG_TAG, "Html: " + args.getString(0));
					Log.v(LOG_TAG, "Html start:" + args.getString(0).substring(0, 30));
					Log.v(LOG_TAG, "Html end:" + args.getString(0).substring(args.getString(0).length() - 30));
				}
				
				if( args.getString(1) != null && args.getString(1) != "null" )
					this.tmpPdfName = args.getString(1);  
				
				final Html2pdf self = this;
				final String content = args.optString(0, "<html></html>");
		        this.callbackContext = callbackContext;
				PageSizes pageSize = PageSizes.ValueOf(args.getString(2));
				PageOrientations pageOrientation = PageOrientation.ValueOf(args.getString(3));
				
		        cordova.getActivity().runOnUiThread( new Runnable() {
		            public void run()
					{
		            	if( Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ) // Android 4.4
		            	{
		            		/*
			            	 * None-Kitkat pdf creation (Android < 4.4)
			            	 */
		            		
			                self.loadContentIntoWebView(content);
		            	}
		            	else
		            	{
			            	/*
			            	 * Kitkat pdf creation by using the android print framework (Android >= 4.4)
			            	 */
		            		
							// Create a WebView object specifically for printing
							WebView page = new WebView(cordova.getActivity());
							page.getSettings().setJavaScriptEnabled(false);
							page.setDrawingCacheEnabled(true);
					        // Auto-scale the content to the webview's width.
							page.getSettings().setLoadWithOverviewMode(true);
							page.getSettings().setUseWideViewPort(true);
							page.setInitialScale(0);
					        // Disable android text auto fit behaviour
							page.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		            		if( showWebViewForDebugging )
	            	        {
		            			page.setVisibility(View.VISIBLE);
	            	        } else {
	            	        	page.setVisibility(View.INVISIBLE);
	            	        }
		            		
							// self.cordova.getActivity().addContentView(webView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		            		page.setWebViewClient( new WebViewClient()
							{
									public boolean shouldOverrideUrlLoading(WebView view, String url) {
										return false;
									}
			
									@Override
									public void onPageFinished(WebView view, String url)
									{
										
										// Get a PrintManager instance
										PrintManager printManager = (PrintManager) self.cordova.getActivity()
												.getSystemService(Context.PRINT_SERVICE);
			
										// Get a print adapter instance
										PrintDocumentAdapter printAdapter = view.createPrintDocumentAdapter();
										
						                // Get a print builder attributes instance
						                PrintAttributes.Builder builder = new PrintAttributes.Builder();
						                builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
						                // builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
						                // builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);
						                // builder.setResolution(new PrintAttributes.Resolution("default", self.tmpPdfName, 600, 600));
						                
						                // send success result to cordova
						                PluginResult result = new PluginResult(PluginResult.Status.OK);
						                result.setKeepCallback(false); 
					                    self.callbackContext.sendPluginResult(result);
						                
						                // Create & send a print job
					                    File filePdf = new File(self.tmpPdfName);
										printManager.print(filePdf.getName(), printAdapter, builder.build());
										
										
										
									}
							});
							
							// Reverse engineer base url (assets/www) from the cordova webView url
					        String baseURL = self.webView.getUrl();
					        baseURL        = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);
					        
					        // Load content into the print webview
					        if( showWebViewForDebugging )
	            	        {
					        	cordova.getActivity().addContentView(page, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	            	        }
				            page.loadDataWithBaseURL(baseURL, content, "text/html", "utf-8", null);
		            	}
		            }
		        });

		        
		        // send "no-result" result to delay result handling
		        PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT); 
		        pluginResult.setKeepCallback(true); 
		        callbackContext.sendPluginResult(pluginResult);
		        
				return true;
			}
			return false;
		}
		catch (JSONException e)
		{
			// TODO: signal JSON problem to JS
			//callbackContext.error("Problem with JSON");
			return false;
		}
    }


	/**
	 *
	 * Clean up and close all open files.
	 *
	 */
	@Override
	public void onDestroy()
	{
		// ToDo: clean up.
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------
	

    /**
     * Loads the html content into a WebView, saves it as a single multi page pdf file and
     * calls startPdfApp() once it´s done.
     */
    private void loadContentIntoWebView (String content)
    {
              Activity ctx = cordova.getActivity();
        final WebView page = new Html2PdfWebView(ctx);
        final Html2pdf self = this;
        
        if( showWebViewForDebugging )
        {
        	page.setVisibility(View.VISIBLE);
        } else {
        	page.setVisibility(View.INVISIBLE);
        }
        page.getSettings().setJavaScriptEnabled(false);
        page.setDrawingCacheEnabled(true);
        // Don´t auto-scale the content to the webview's width.
        page.getSettings().setLoadWithOverviewMode(false);
        page.getSettings().setUseWideViewPort(false);
        page.setInitialScale(100);
        // Disable android text auto fit behaviour
        page.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        
        page.setWebViewClient( new WebViewClient() {
            @Override
            public void onPageFinished(final WebView page, String url) {
                new Handler().postDelayed( new Runnable() {
                  @Override
                  public void run()
                  {
                        // slice the web screenshot into pages and save as pdf
                	  	Bitmap b = getWebViewAsBitmap(page);
                	  	if( b != null )
                	  	{
	                        File tmpFile = self.saveWebViewAsPdf(b);
                    		
	                        b.recycle();
	                        
	                        // add pdf as stream to the print intent
	                        Intent pdfViewIntent = new Intent(Intent.ACTION_VIEW);
	                        pdfViewIntent.setDataAndNormalize(Uri.fromFile(tmpFile));
	                        pdfViewIntent.setType("application/pdf");
	
	                        // remove the webview
	                        if( !self.showWebViewForDebugging )
	                        {
	                        	ViewGroup vg = (ViewGroup)(page.getParent());
	                        	vg.removeView(page);
	                        }
	                        
		                    // add file to media scanner
		                    MediaScannerConnection.scanFile(
	                    		self.cordova.getActivity(),
	                    		new String[]{tmpFile.getAbsolutePath()},
	                    		null,
	                    		new OnScanCompletedListener() {
	                    		   @Override
	                    		   public void onScanCompleted(String path, Uri uri) {
	                    		      Log.v(LOG_TAG, "file '" + path + "' was scanned seccessfully: " + uri);
	                    		   }
	                    		}
                    		);
	                        
	                        // start the pdf viewer app (trigger the pdf view intent)
		                    PluginResult result;
		                    boolean success = false; 
		                    if( self.canHandleIntent(self.cordova.getActivity(), pdfViewIntent) )
		                    {
			                    try
			                    {
			                    	self.cordova.startActivityForResult(self, pdfViewIntent, 0);
				                    success = true;
			                    }
			                    catch( ActivityNotFoundException e )
			                    {
			                    	success = false;
			                    }
		                    }
		                    if( success )
		                    {
		                    	// send success result to cordova
				                result = new PluginResult(PluginResult.Status.OK);
				                result.setKeepCallback(false); 
			                    self.callbackContext.sendPluginResult(result);
		                    }
		                    else
		                    {
		                    	// send error
		                        result = new PluginResult(PluginResult.Status.ERROR, "activity_not_found");
		                        result.setKeepCallback(false);
		                        self.callbackContext.sendPluginResult(result);
		                    }
                        }
                  }
                }, 500);
            }
        });

        // Set base URI to the assets/www folder
        String baseURL = webView.getUrl();
               baseURL = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);

        /** We make it this small on purpose (is resized after page load has finished).
         *  Making it small in the beginning has some effects on the html <body> (body
         *  width will always remain 100 if not set explicitly).
         */
        if( !showWebViewForDebugging )
        {
        	ctx.addContentView(page, new ViewGroup.LayoutParams(100, 100));
        }
        else
        {
        	ctx.addContentView(page, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        page.loadDataWithBaseURL(baseURL, content, "text/html", "utf-8", null);
    }
    
    public static final String MIME_TYPE_PDF = "application/pdf";

	/**
	 * Check if the supplied context can handle the given intent.
	 *
	 * @param context
	 * @param intent
	 * @return boolean
	 */
	public boolean canHandleIntent(Context context, Intent intent)
	{
	    PackageManager packageManager = context.getPackageManager();
	    return (packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0);
	}
    
    /**
     * Takes a WebView and returns a Bitmap representation of it (takes a "screenshot").
     * @param WebView
     * @return Bitmap
     */
    Bitmap getWebViewAsBitmap(WebView view)
    {
    	Bitmap b; 
    	
    	// prepare drawing cache
    	view.setDrawingCacheEnabled(true);
    	view.buildDrawingCache();
    			
        //Get the dimensions of the view so we can re-layout the view at its current size
        //and create a bitmap of the same size 
        int width = ((Html2PdfWebView) view).getContentWidth();
        int height = view.getContentHeight();

        if( width == 0 || height == 0 )
        {
            // return error answer to cordova
        	String msg = "Width or height of webview content is 0. Webview to bitmap conversion failed.";
        	Log.e(LOG_TAG, msg );
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, msg);
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
            
        	return null;
        }
        
        Log.v(LOG_TAG, "Html2Pdf.getWebViewAsBitmap -> Content width: " + width + ", height: " + height );

        //Cause the view to re-layout
        view.measure(width, height);
        view.layout(0, 0, width, height);

        //Create a bitmap backed Canvas to draw the view into
        b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        // draw the view into the canvas
        view.draw(c);
        
        return b;
    }

    /**
     * Slices the screenshot into pages, merges those into a single pdf
     * and saves it in the public accessible /sdcard dir.
     */
    private File saveWebViewAsPdf(Bitmap screenshot) {
        try {
        	
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/" + this.publicTmpDir + "/");
            dir.mkdirs();
            File file;
            FileOutputStream stream;
            
            // creat nomedia file to avoid indexing tmp files
            File noMediaFile = new File(dir.getAbsolutePath() + "/", ".nomedia");
            if( !noMediaFile.exists() ) 
            {
            	noMediaFile.createNewFile();
            }
			
			double pageWidth = 0;
			double pageHeight = 0;
			
            switch (pageOrientation)
			{
				case PageOrientations.Portrait:
					switch (pageSize)
					{
						case PageSizes.Letter:
							pageWidth  = PageSizes.LETTER.getWidth()  * 0.85;
							pageHeight = PageSizes.LETTER.getHeight() * 0.80;
							break;
						case PageSizes.Note:
							pageWidth  = PageSizes.NOTE.getWidth()  * 0.85;
							pageHeight = PageSizes.NOTE.getHeight() * 0.80;
							break;
						case PageSizes.Legal:
							pageWidth  = PageSizes.LEGAL.getWidth()  * 0.85;
							pageHeight = PageSizes.LEGAL.getHeight() * 0.80;
							break;
						case PageSizes.Tabloid:
							pageWidth  = PageSizes.TABLOID.getWidth()  * 0.85;
							pageHeight = PageSizes.TABLOID.getHeight() * 0.80;
							break;
						case PageSizes.Executive:
							pageWidth  = PageSizes.EXECUTIVE.getWidth()  * 0.85;
							pageHeight = PageSizes.EXECUTIVE.getHeight() * 0.80;
							break;
						case PageSizes.Postcard:
							pageWidth  = PageSizes.POSTCARD.getWidth()  * 0.85;
							pageHeight = PageSizes.POSTCARD.getHeight() * 0.80;
							break;
						case PageSizes.A0:
							pageWidth  = PageSizes.A0.getWidth()  * 0.85;
							pageHeight = PageSizes.A0.getHeight() * 0.80;
							break;
						case PageSizes.A1:
							pageWidth  = PageSizes.A1.getWidth()  * 0.85;
							pageHeight = PageSizes.A1.getHeight() * 0.80;
							break;
						case PageSizes.A2:
							pageWidth  = PageSizes.A2.getWidth()  * 0.85;
							pageHeight = PageSizes.A2.getHeight() * 0.80;
							break;
						case PageSizes.A3:
							pageWidth  = PageSizes.A3.getWidth()  * 0.85;
							pageHeight = PageSizes.A3.getHeight() * 0.80;
							break;
						case PageSizes.A4:
							pageWidth  = PageSizes.A4.getWidth()  * 0.85;
							pageHeight = PageSizes.A4.getHeight() * 0.80;
							break;
						case PageSizes.A5:
							pageWidth  = PageSizes.A5.getWidth()  * 0.85;
							pageHeight = PageSizes.A5.getHeight() * 0.80;
							break;
						case PageSizes.A6:
							pageWidth  = PageSizes.A6.getWidth()  * 0.85;
							pageHeight = PageSizes.A6.getHeight() * 0.80;
							break;
						case PageSizes.A7:
							pageWidth  = PageSizes.A7.getWidth()  * 0.85;
							pageHeight = PageSizes.A7.getHeight() * 0.80;
							break;
						case PageSizes.A8:
							pageWidth  = PageSizes.A8.getWidth()  * 0.85;
							pageHeight = PageSizes.A8.getHeight() * 0.80;
							break;
						case PageSizes.A9:
							pageWidth  = PageSizes.A9.getWidth()  * 0.85;
							pageHeight = PageSizes.A9.getHeight() * 0.80;
							break;
						case PageSizes.A10:
							pageWidth  = PageSizes.A10.getWidth()  * 0.85;
							pageHeight = PageSizes.A10.getHeight() * 0.80;
							break;
						case PageSizes.B0:
							pageWidth  = PageSizes.B0.getWidth()  * 0.85;
							pageHeight = PageSizes.B0.getHeight() * 0.80;
							break;
						case PageSizes.B1:
							pageWidth  = PageSizes.B1.getWidth()  * 0.85;
							pageHeight = PageSizes.B1.getHeight() * 0.80;
							break;
						case PageSizes.B2:
							pageWidth  = PageSizes.B2.getWidth()  * 0.85;
							pageHeight = PageSizes.B2.getHeight() * 0.80;
							break;
						case PageSizes.B3:
							pageWidth  = PageSizes.B3.getWidth()  * 0.85;
							pageHeight = PageSizes.B3.getHeight() * 0.80;
							break;
						case PageSizes.B4:
							pageWidth  = PageSizes.B4.getWidth()  * 0.85;
							pageHeight = PageSizes.B4.getHeight() * 0.80;
							break;
						case PageSizes.B5:
							pageWidth  = PageSizes.B5.getWidth()  * 0.85;
							pageHeight = PageSizes.B5.getHeight() * 0.80;
							break;
						case PageSizes.B6:
							pageWidth  = PageSizes.B6.getWidth()  * 0.85;
							pageHeight = PageSizes.B6.getHeight() * 0.80;
							break;
						case PageSizes.B7:
							pageWidth  = PageSizes.B7.getWidth()  * 0.85;
							pageHeight = PageSizes.B7.getHeight() * 0.80;
							break;
						case PageSizes.B8:
							pageWidth  = PageSizes.B8.getWidth()  * 0.85;
							pageHeight = PageSizes.B8.getHeight() * 0.80;
							break;
						case PageSizes.B9:
							pageWidth  = PageSizes.B9.getWidth()  * 0.85;
							pageHeight = PageSizes.B9.getHeight() * 0.80;
							break;
						case PageSizes.B10:
							pageWidth  = PageSizes.B10.getWidth()  * 0.85;
							pageHeight = PageSizes.B10.getHeight() * 0.80;
							break;
						case PageSizes.ArchE:
							pageWidth  = PageSizes.ARCH_E.getWidth()  * 0.85;
							pageHeight = PageSizes.ARCH_E.getHeight() * 0.80;
							break;
						case PageSizes.ArchD:
							pageWidth  = PageSizes.ARCH_D.getWidth()  * 0.85;
							pageHeight = PageSizes.ARCH_D.getHeight() * 0.80;
							break;
						case PageSizes.ArchC:
							pageWidth  = PageSizes.ARCH_C.getWidth()  * 0.85;
							pageHeight = PageSizes.ARCH_C.getHeight() * 0.80;
							break;
						case PageSizes.ArchB:
							pageWidth  = PageSizes.ARCH_B.getWidth()  * 0.85;
							pageHeight = PageSizes.ARCH_B.getHeight() * 0.80;
							break;
						case PageSizes.ArchA:
							pageWidth  = PageSizes.ARCH_A.getWidth()  * 0.85;
							pageHeight = PageSizes.ARCH_A.getHeight() * 0.80;
							break;
						case PageSizes.FLSA:
							pageWidth  = PageSizes.FLSA.getWidth()  * 0.85;
							pageHeight = PageSizes.FLSA.getHeight() * 0.80;
							break;
						case PageSizes.FLSE:
							pageWidth  = PageSizes.FLSE.getWidth()  * 0.85;
							pageHeight = PageSizes.FLSE.getHeight() * 0.80;
							break;
						case PageSizes.HalfLetter:
							pageWidth  = PageSizes.HALFLETTER.getWidth()  * 0.85;
							pageHeight = PageSizes.HALFLETTER.getHeight() * 0.80;
							break;
						case PageSizes.Ledger:
							pageWidth  = PageSizes._11X17.getWidth()  * 0.85;
							pageHeight = PageSizes._11X17.getHeight() * 0.80;
							break;
						case PageSizes.ID1:
							pageWidth  = PageSizes.ID_1.getWidth()  * 0.85;
							pageHeight = PageSizes.ID_1.getHeight() * 0.80;
							break;
						case PageSizes.ID2:
							pageWidth  = PageSizes.ID_2.getWidth()  * 0.85;
							pageHeight = PageSizes.ID_2.getHeight() * 0.80;
							break;
						case PageSizes.ID3:
							pageWidth  = PageSizes.ID_3.getWidth()  * 0.85;
							pageHeight = PageSizes.ID_3.getHeight() * 0.80;
							break;
						case PageSizes.CrownQuarto:
							pageWidth  = PageSizes.CROWN_QUARTO.getWidth()  * 0.85;
							pageHeight = PageSizes.CROWN_QUARTO.getHeight() * 0.80;
							break;
						case PageSizes.LargeCrownQuarto:
							pageWidth  = PageSizes.LARGE_CROWN_QUARTO.getWidth()  * 0.85;
							pageHeight = PageSizes.LARGE_CROWN_QUARTO.getHeight() * 0.80;
							break;
						case PageSizes.DemyQuarto:
							pageWidth  = PageSizes.DEMY_QUARTO.getWidth()  * 0.85;
							pageHeight = PageSizes.DEMY_QUARTO.getHeight() * 0.80;
							break;
						case PageSizes.RoyalQuarto:
							pageWidth  = PageSizes.ROYAL_QUARTO.getWidth()  * 0.85;
							pageHeight = PageSizes.ROYAL_QUARTO.getHeight() * 0.80;
							break;
						case PageSizes.CrownOctavo:
							pageWidth  = PageSizes.CROWN_OCTAVO.getWidth()  * 0.85;
							pageHeight = PageSizes.CROWN_OCTAVO.getHeight() * 0.80;
							break;
						case PageSizes.LargeCrownOctavo:
							pageWidth  = PageSizes.LARGE_CROWN_OCTAVO.getWidth()  * 0.85;
							pageHeight = PageSizes.LARGE_CROWN_OCTAVO.getHeight() * 0.80;
							break;
						case PageSizes.DemyOctavo:
							pageWidth  = PageSizes.DEMY_OCTAVO.getWidth()  * 0.85;
							pageHeight = PageSizes.DEMY_OCTAVO.getHeight() * 0.80;
							break;
						case PageSizes.RoyalOctavo:
							pageWidth  = PageSizes.ROYAL_OCTAVO.getWidth()  * 0.85;
							pageHeight = PageSizes.ROYAL_OCTAVO.getHeight() * 0.80;
							break;
						case PageSizes.SmallPaperback:
							pageWidth  = PageSizes.SMALL_PAPERBACK.getWidth()  * 0.85;
							pageHeight = PageSizes.SMALL_PAPERBACK.getHeight() * 0.80;
							break;
						case PageSizes.PenguinSmallPaperback:
							pageWidth  = PageSizes.PENGUIN_SMALL_PAPERBACK.getWidth()  * 0.85;
							pageHeight = PageSizes.PENGUIN_SMALL_PAPERBACK.getHeight() * 0.80;
							break;
						case PageSizes.PenguinLargePaperback:
							pageWidth  = PageSizes.PENGUIN_LARGE_PAPERBACK.getWidth()  * 0.85;
							pageHeight = PageSizes.PENGUIN_LARGE_PAPERBACK.getHeight() * 0.80;
							break;	
						default:
							pageWidth  = PageSizes.LETTER.getWidth()  * 0.85;
							pageHeight = PageSizes.LETTER.getHeight() * 0.80;
							break;
					}
					break;
				case PageOrientation.Landscape:
					switch (pageSize)
					{
						case PageSizes.Letter:
							pageWidth  = PageSizes.LETTER.getHeight()  * 0.85;
							pageHeight = PageSizes.LETTER.getWidth() * 0.80;
							break;
						case PageSizes.Note:
							pageWidth  = PageSizes.NOTE.getHeight()  * 0.85;
							pageHeight = PageSizes.NOTE.getWidth() * 0.80;
							break;
						case PageSizes.Legal:
							pageWidth  = PageSizes.LEGAL.getHeight()  * 0.85;
							pageHeight = PageSizes.LEGAL.getWidth() * 0.80;
							break;
						case PageSizes.Tabloid:
							pageWidth  = PageSizes.TABLOID.getHeight()  * 0.85;
							pageHeight = PageSizes.TABLOID.getWidth() * 0.80;
							break;
						case PageSizes.Executive:
							pageWidth  = PageSizes.EXECUTIVE.getHeight()  * 0.85;
							pageHeight = PageSizes.EXECUTIVE.getWidth() * 0.80;
							break;
						case PageSizes.Postcard:
							pageWidth  = PageSizes.POSTCARD.getHeight()  * 0.85;
							pageHeight = PageSizes.POSTCARD.getWidth() * 0.80;
							break;
						case PageSizes.A0:
							pageWidth  = PageSizes.A0.getHeight()  * 0.85;
							pageHeight = PageSizes.A0.getWidth() * 0.80;
							break;
						case PageSizes.A1:
							pageWidth  = PageSizes.A1.getHeight()  * 0.85;
							pageHeight = PageSizes.A1.getWidth() * 0.80;
							break;
						case PageSizes.A2:
							pageWidth  = PageSizes.A2.getHeight()  * 0.85;
							pageHeight = PageSizes.A2.getWidth() * 0.80;
							break;
						case PageSizes.A3:
							pageWidth  = PageSizes.A3.getHeight()  * 0.85;
							pageHeight = PageSizes.A3.getWidth() * 0.80;
							break;
						case PageSizes.A4:
							pageWidth  = PageSizes.A4.getHeight()  * 0.85;
							pageHeight = PageSizes.A4.getWidth() * 0.80;
							break;
						case PageSizes.A5:
							pageWidth  = PageSizes.A5.getHeight()  * 0.85;
							pageHeight = PageSizes.A5.getWidth() * 0.80;
							break;
						case PageSizes.A6:
							pageWidth  = PageSizes.A6.getHeight()  * 0.85;
							pageHeight = PageSizes.A6.getWidth() * 0.80;
							break;
						case PageSizes.A7:
							pageWidth  = PageSizes.A7.getHeight()  * 0.85;
							pageHeight = PageSizes.A7.getWidth() * 0.80;
							break;
						case PageSizes.A8:
							pageWidth  = PageSizes.A8.getHeight()  * 0.85;
							pageHeight = PageSizes.A8.getWidth() * 0.80;
							break;
						case PageSizes.A9:
							pageWidth  = PageSizes.A9.getHeight()  * 0.85;
							pageHeight = PageSizes.A9.getWidth() * 0.80;
							break;
						case PageSizes.A10:
							pageWidth  = PageSizes.A10.getHeight()  * 0.85;
							pageHeight = PageSizes.A10.getWidth() * 0.80;
							break;
						case PageSizes.B0:
							pageWidth  = PageSizes.B0.getHeight()  * 0.85;
							pageHeight = PageSizes.B0.getWidth() * 0.80;
							break;
						case PageSizes.B1:
							pageWidth  = PageSizes.B1.getHeight()  * 0.85;
							pageHeight = PageSizes.B1.getWidth() * 0.80;
							break;
						case PageSizes.B2:
							pageWidth  = PageSizes.B2.getHeight()  * 0.85;
							pageHeight = PageSizes.B2.getWidth() * 0.80;
							break;
						case PageSizes.B3:
							pageWidth  = PageSizes.B3.getHeight()  * 0.85;
							pageHeight = PageSizes.B3.getWidth() * 0.80;
							break;
						case PageSizes.B4:
							pageWidth  = PageSizes.B4.getHeight()  * 0.85;
							pageHeight = PageSizes.B4.getWidth() * 0.80;
							break;
						case PageSizes.B5:
							pageWidth  = PageSizes.B5.getHeight()  * 0.85;
							pageHeight = PageSizes.B5.getWidth() * 0.80;
							break;
						case PageSizes.B6:
							pageWidth  = PageSizes.B6.getHeight()  * 0.85;
							pageHeight = PageSizes.B6.getWidth() * 0.80;
							break;
						case PageSizes.B7:
							pageWidth  = PageSizes.B7.getHeight()  * 0.85;
							pageHeight = PageSizes.B7.getWidth() * 0.80;
							break;
						case PageSizes.B8:
							pageWidth  = PageSizes.B8.getHeight()  * 0.85;
							pageHeight = PageSizes.B8.getWidth() * 0.80;
							break;
						case PageSizes.B9:
							pageWidth  = PageSizes.B9.getHeight()  * 0.85;
							pageHeight = PageSizes.B9.getWidth() * 0.80;
							break;
						case PageSizes.B10:
							pageWidth  = PageSizes.B10.getHeight()  * 0.85;
							pageHeight = PageSizes.B10.getWidth() * 0.80;
							break;
						case PageSizes.ArchE:
							pageWidth  = PageSizes.ARCH_E.getHeight()  * 0.85;
							pageHeight = PageSizes.ARCH_E.getWidth() * 0.80;
							break;
						case PageSizes.ArchD:
							pageWidth  = PageSizes.ARCH_D.getHeight()  * 0.85;
							pageHeight = PageSizes.ARCH_D.getWidth() * 0.80;
							break;
						case PageSizes.ArchC:
							pageWidth  = PageSizes.ARCH_C.getHeight()  * 0.85;
							pageHeight = PageSizes.ARCH_C.getWidth() * 0.80;
							break;
						case PageSizes.ArchB:
							pageWidth  = PageSizes.ARCH_B.getHeight()  * 0.85;
							pageHeight = PageSizes.ARCH_B.getWidth() * 0.80;
							break;
						case PageSizes.ArchA:
							pageWidth  = PageSizes.ARCH_A.getHeight()  * 0.85;
							pageHeight = PageSizes.ARCH_A.getWidth() * 0.80;
							break;
						case PageSizes.FLSA:
							pageWidth  = PageSizes.FLSA.getHeight()  * 0.85;
							pageHeight = PageSizes.FLSA.getWidth() * 0.80;
							break;
						case PageSizes.FLSE:
							pageWidth  = PageSizes.FLSE.getHeight()  * 0.85;
							pageHeight = PageSizes.FLSE.getWidth() * 0.80;
							break;
						case PageSizes.HalfLetter:
							pageWidth  = PageSizes.HALFLETTER.getHeight()  * 0.85;
							pageHeight = PageSizes.HALFLETTER.getWidth() * 0.80;
							break;
						case PageSizes.Ledger:
							pageWidth  = PageSizes._11X17.getHeight()  * 0.85;
							pageHeight = PageSizes._11X17.getWidth() * 0.80;
							break;
						case PageSizes.ID1:
							pageWidth  = PageSizes.ID_1.getHeight()  * 0.85;
							pageHeight = PageSizes.ID_1.getWidth() * 0.80;
							break;
						case PageSizes.ID2:
							pageWidth  = PageSizes.ID_2.getHeight()  * 0.85;
							pageHeight = PageSizes.ID_2.getWidth() * 0.80;
							break;
						case PageSizes.ID3:
							pageWidth  = PageSizes.ID_3.getHeight()  * 0.85;
							pageHeight = PageSizes.ID_3.getWidth() * 0.80;
							break;
						case PageSizes.CrownQuarto:
							pageWidth  = PageSizes.CROWN_QUARTO.getHeight()  * 0.85;
							pageHeight = PageSizes.CROWN_QUARTO.getWidth() * 0.80;
							break;
						case PageSizes.LargeCrownQuarto:
							pageWidth  = PageSizes.LARGE_CROWN_QUARTO.getHeight()  * 0.85;
							pageHeight = PageSizes.LARGE_CROWN_QUARTO.getWidth() * 0.80;
							break;
						case PageSizes.DemyQuarto:
							pageWidth  = PageSizes.DEMY_QUARTO.getHeight()  * 0.85;
							pageHeight = PageSizes.DEMY_QUARTO.getWidth() * 0.80;
							break;
						case PageSizes.RoyalQuarto:
							pageWidth  = PageSizes.ROYAL_QUARTO.getHeight()  * 0.85;
							pageHeight = PageSizes.ROYAL_QUARTO.getWidth() * 0.80;
							break;
						case PageSizes.CrownOctavo:
							pageWidth  = PageSizes.CROWN_OCTAVO.getHeight()  * 0.85;
							pageHeight = PageSizes.CROWN_OCTAVO.getWidth() * 0.80;
							break;
						case PageSizes.LargeCrownOctavo:
							pageWidth  = PageSizes.LARGE_CROWN_OCTAVO.getHeight()  * 0.85;
							pageHeight = PageSizes.LARGE_CROWN_OCTAVO.getWidth() * 0.80;
							break;
						case PageSizes.DemyOctavo:
							pageWidth  = PageSizes.DEMY_OCTAVO.getHeight()  * 0.85;
							pageHeight = PageSizes.DEMY_OCTAVO.getWidth() * 0.80;
							break;
						case PageSizes.RoyalOctavo:
							pageWidth  = PageSizes.ROYAL_OCTAVO.getHeight()  * 0.85;
							pageHeight = PageSizes.ROYAL_OCTAVO.getWidth() * 0.80;
							break;
						case PageSizes.SmallPaperback:
							pageWidth  = PageSizes.SMALL_PAPERBACK.getHeight()  * 0.85;
							pageHeight = PageSizes.SMALL_PAPERBACK.getWidth() * 0.80;
							break;
						case PageSizes.PenguinSmallPaperback:
							pageWidth  = PageSizes.PENGUIN_SMALL_PAPERBACK.getHeight()  * 0.85;
							pageHeight = PageSizes.PENGUIN_SMALL_PAPERBACK.getWidth() * 0.80;
							break;
						case PageSizes.PenguinLargePaperback:
							pageWidth  = PageSizes.PENGUIN_LARGE_PAPERBACK.getHeight()  * 0.85;
							pageHeight = PageSizes.PENGUIN_LARGE_PAPERBACK.getWidth() * 0.80;
							break;	
						default:
							pageWidth  = PageSizes.LETTER.getHeight()  * 0.85;
							pageHeight = PageSizes.LETTER.getWidth() * 0.80;
							break;
					}
					break;
				default:
					pageWidth  = PageSizes.LETTER.getWidth()  * 0.85;
					pageHeight = PageSizes.LETTER.getHeight() * 0.80;
					break;
			}
			
            double pageHeightToWithRelation = pageHeight / pageWidth; // e.g.: 1.33 (4/3)
            
            Bitmap currPage;
            int totalSize  = screenshot.getHeight();
            int currPos = 0;
            int currPageCount = 0;
            int sliceWidth = screenshot.getWidth();
            int sliceHeight = (int) Math.round(sliceWidth * pageHeightToWithRelation);
            while( totalSize > currPos && currPageCount < 100  ) // max 100 pages
            {
            	currPageCount++;
            	
            	Log.v(LOG_TAG, "Creating page nr. " + currPageCount );
            	
            	// slice bitmap
            	currPage = Bitmap.createBitmap(screenshot, 0, currPos, sliceWidth, (int) Math.min( sliceHeight, totalSize - currPos ));
            	
            	// save page as png
            	stream = new FileOutputStream( new File(dir, "pdf-page-"+currPageCount+".png") );
            	currPage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                
                // move current position indicator
            	currPos += sliceHeight;
            	
            	currPage.recycle();
            }
            
            // create pdf
            Document document = new Document();
            File filePdf = new File(sdCard.getAbsolutePath() + "/" + this.tmpPdfName); // change the output name of the pdf here
            // create dirs if necessary
            if( this.tmpPdfName.contains("/") )
            {
            	File filePdfDir = new File(filePdf.getAbsolutePath().substring(0, filePdf.getAbsolutePath().lastIndexOf("/"))); // get  the dir portion
            	filePdfDir.mkdirs();
            }
            PdfWriter.getInstance(document,new FileOutputStream(filePdf));
            document.open();
            for( int i=1; i<=currPageCount; ++i )
            {
            	file = new File(dir, "pdf-page-"+i+".png");
            	Image image = Image.getInstance (file.getAbsolutePath());
                image.scaleToFit( (float)pageWidth, 9999);
            	image.setAlignment(Element.ALIGN_CENTER);
            	document.add(image);
            	document.newPage();
            }
            document.close();
            
            // delete tmp image files
            for( int i=1; i<=currPageCount; ++i )
            {
            	file = new File(dir, "pdf-page-"+i+".png");
            	file.delete();
            }
            
            return filePdf;
            
        } catch (IOException e) {
        	Log.e(LOG_TAG, "ERROR: " + e.getMessage());
            e.printStackTrace();
            // return error answer to cordova
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        } catch (DocumentException e) {
        	Log.e(LOG_TAG, "ERROR: " + e.getMessage());
			e.printStackTrace();
            // return error answer to cordova
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
		}
        
        Log.v(LOG_TAG, "Uncaught ERROR!");

        return null;
    }


}

class Html2PdfWebView extends WebView {
    public Html2PdfWebView(Context context) {
		super(context);
	}
    
    public int getContentWidth()
    {
    	return this.computeHorizontalScrollRange();
    }
}
