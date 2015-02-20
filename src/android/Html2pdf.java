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
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

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
	private String pageSize = "";
	private String pageOrientation = "";
	
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
				pageSize = args.getString(2);
				pageOrientation = args.getString(3);
				
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
				case "Portrait":
					switch (pageSize)
					{
						case "Letter":
							pageWidth  = PageSize.LETTER.getWidth()  * 0.85;
							pageHeight = PageSize.LETTER.getHeight() * 0.80;
							break;
						case "Note":
							pageWidth  = PageSize.NOTE.getWidth()  * 0.85;
							pageHeight = PageSize.NOTE.getHeight() * 0.80;
							break;
						case "Legal":
							pageWidth  = PageSize.LEGAL.getWidth()  * 0.85;
							pageHeight = PageSize.LEGAL.getHeight() * 0.80;
							break;
						case "Tabloid":
							pageWidth  = PageSize.TABLOID.getWidth()  * 0.85;
							pageHeight = PageSize.TABLOID.getHeight() * 0.80;
							break;
						case "Executive":
							pageWidth  = PageSize.EXECUTIVE.getWidth()  * 0.85;
							pageHeight = PageSize.EXECUTIVE.getHeight() * 0.80;
							break;
						case "Postcard":
							pageWidth  = PageSize.POSTCARD.getWidth()  * 0.85;
							pageHeight = PageSize.POSTCARD.getHeight() * 0.80;
							break;
						case "A0":
							pageWidth  = PageSize.A0.getWidth()  * 0.85;
							pageHeight = PageSize.A0.getHeight() * 0.80;
							break;
						case "A1":
							pageWidth  = PageSize.A1.getWidth()  * 0.85;
							pageHeight = PageSize.A1.getHeight() * 0.80;
							break;
						case "A2":
							pageWidth  = PageSize.A2.getWidth()  * 0.85;
							pageHeight = PageSize.A2.getHeight() * 0.80;
							break;
						case "A3":
							pageWidth  = PageSize.A3.getWidth()  * 0.85;
							pageHeight = PageSize.A3.getHeight() * 0.80;
							break;
						case "A4":
							pageWidth  = PageSize.A4.getWidth()  * 0.85;
							pageHeight = PageSize.A4.getHeight() * 0.80;
							break;
						case "A5":
							pageWidth  = PageSize.A5.getWidth()  * 0.85;
							pageHeight = PageSize.A5.getHeight() * 0.80;
							break;
						case "A6":
							pageWidth  = PageSize.A6.getWidth()  * 0.85;
							pageHeight = PageSize.A6.getHeight() * 0.80;
							break;
						case "A7":
							pageWidth  = PageSize.A7.getWidth()  * 0.85;
							pageHeight = PageSize.A7.getHeight() * 0.80;
							break;
						case "A8":
							pageWidth  = PageSize.A8.getWidth()  * 0.85;
							pageHeight = PageSize.A8.getHeight() * 0.80;
							break;
						case "A9":
							pageWidth  = PageSize.A9.getWidth()  * 0.85;
							pageHeight = PageSize.A9.getHeight() * 0.80;
							break;
						case "A10":
							pageWidth  = PageSize.A10.getWidth()  * 0.85;
							pageHeight = PageSize.A10.getHeight() * 0.80;
							break;
						case "B0":
							pageWidth  = PageSize.B0.getWidth()  * 0.85;
							pageHeight = PageSize.B0.getHeight() * 0.80;
							break;
						case "B1":
							pageWidth  = PageSize.B1.getWidth()  * 0.85;
							pageHeight = PageSize.B1.getHeight() * 0.80;
							break;
						case "B2":
							pageWidth  = PageSize.B2.getWidth()  * 0.85;
							pageHeight = PageSize.B2.getHeight() * 0.80;
							break;
						case "B3":
							pageWidth  = PageSize.B3.getWidth()  * 0.85;
							pageHeight = PageSize.B3.getHeight() * 0.80;
							break;
						case "B4":
							pageWidth  = PageSize.B4.getWidth()  * 0.85;
							pageHeight = PageSize.B4.getHeight() * 0.80;
							break;
						case "B5":
							pageWidth  = PageSize.B5.getWidth()  * 0.85;
							pageHeight = PageSize.B5.getHeight() * 0.80;
							break;
						case "B6":
							pageWidth  = PageSize.B6.getWidth()  * 0.85;
							pageHeight = PageSize.B6.getHeight() * 0.80;
							break;
						case "B7":
							pageWidth  = PageSize.B7.getWidth()  * 0.85;
							pageHeight = PageSize.B7.getHeight() * 0.80;
							break;
						case "B8":
							pageWidth  = PageSize.B8.getWidth()  * 0.85;
							pageHeight = PageSize.B8.getHeight() * 0.80;
							break;
						case "B9":
							pageWidth  = PageSize.B9.getWidth()  * 0.85;
							pageHeight = PageSize.B9.getHeight() * 0.80;
							break;
						case "B10":
							pageWidth  = PageSize.B10.getWidth()  * 0.85;
							pageHeight = PageSize.B10.getHeight() * 0.80;
							break;
						case "ArchE":
							pageWidth  = PageSize.ARCH_E.getWidth()  * 0.85;
							pageHeight = PageSize.ARCH_E.getHeight() * 0.80;
							break;
						case "ArchD":
							pageWidth  = PageSize.ARCH_D.getWidth()  * 0.85;
							pageHeight = PageSize.ARCH_D.getHeight() * 0.80;
							break;
						case "ArchC":
							pageWidth  = PageSize.ARCH_C.getWidth()  * 0.85;
							pageHeight = PageSize.ARCH_C.getHeight() * 0.80;
							break;
						case "ArchB":
							pageWidth  = PageSize.ARCH_B.getWidth()  * 0.85;
							pageHeight = PageSize.ARCH_B.getHeight() * 0.80;
							break;
						case "ArchA":
							pageWidth  = PageSize.ARCH_A.getWidth()  * 0.85;
							pageHeight = PageSize.ARCH_A.getHeight() * 0.80;
							break;
						case "FLSA":
							pageWidth  = PageSize.FLSA.getWidth()  * 0.85;
							pageHeight = PageSize.FLSA.getHeight() * 0.80;
							break;
						case "FLSE":
							pageWidth  = PageSize.FLSE.getWidth()  * 0.85;
							pageHeight = PageSize.FLSE.getHeight() * 0.80;
							break;
						case "HalfLetter":
							pageWidth  = PageSize.HALFLETTER.getWidth()  * 0.85;
							pageHeight = PageSize.HALFLETTER.getHeight() * 0.80;
							break;
						case "Ledger":
							pageWidth  = PageSize._11X17.getWidth()  * 0.85;
							pageHeight = PageSize._11X17.getHeight() * 0.80;
							break;
						case "ID1":
							pageWidth  = PageSize.ID_1.getWidth()  * 0.85;
							pageHeight = PageSize.ID_1.getHeight() * 0.80;
							break;
						case "ID2":
							pageWidth  = PageSize.ID_2.getWidth()  * 0.85;
							pageHeight = PageSize.ID_2.getHeight() * 0.80;
							break;
						case "ID3":
							pageWidth  = PageSize.ID_3.getWidth()  * 0.85;
							pageHeight = PageSize.ID_3.getHeight() * 0.80;
							break;
						case "CrownQuarto":
							pageWidth  = PageSize.CROWN_QUARTO.getWidth()  * 0.85;
							pageHeight = PageSize.CROWN_QUARTO.getHeight() * 0.80;
							break;
						case "LargeCrownQuarto":
							pageWidth  = PageSize.LARGE_CROWN_QUARTO.getWidth()  * 0.85;
							pageHeight = PageSize.LARGE_CROWN_QUARTO.getHeight() * 0.80;
							break;
						case "DemyQuarto":
							pageWidth  = PageSize.DEMY_QUARTO.getWidth()  * 0.85;
							pageHeight = PageSize.DEMY_QUARTO.getHeight() * 0.80;
							break;
						case "RoyalQuarto":
							pageWidth  = PageSize.ROYAL_QUARTO.getWidth()  * 0.85;
							pageHeight = PageSize.ROYAL_QUARTO.getHeight() * 0.80;
							break;
						case "CrownOctavo":
							pageWidth  = PageSize.CROWN_OCTAVO.getWidth()  * 0.85;
							pageHeight = PageSize.CROWN_OCTAVO.getHeight() * 0.80;
							break;
						case "LargeCrownOctavo":
							pageWidth  = PageSize.LARGE_CROWN_OCTAVO.getWidth()  * 0.85;
							pageHeight = PageSize.LARGE_CROWN_OCTAVO.getHeight() * 0.80;
							break;
						case "DemyOctavo":
							pageWidth  = PageSize.DEMY_OCTAVO.getWidth()  * 0.85;
							pageHeight = PageSize.DEMY_OCTAVO.getHeight() * 0.80;
							break;
						case "RoyalOctavo":
							pageWidth  = PageSize.ROYAL_OCTAVO.getWidth()  * 0.85;
							pageHeight = PageSize.ROYAL_OCTAVO.getHeight() * 0.80;
							break;
						case "SmallPaperback":
							pageWidth  = PageSize.SMALL_PAPERBACK.getWidth()  * 0.85;
							pageHeight = PageSize.SMALL_PAPERBACK.getHeight() * 0.80;
							break;
						case "PenguinSmallPaperback":
							pageWidth  = PageSize.PENGUIN_SMALL_PAPERBACK.getWidth()  * 0.85;
							pageHeight = PageSize.PENGUIN_SMALL_PAPERBACK.getHeight() * 0.80;
							break;
						case "PenguinLargePaperback":
							pageWidth  = PageSize.PENGUIN_LARGE_PAPERBACK.getWidth()  * 0.85;
							pageHeight = PageSize.PENGUIN_LARGE_PAPERBACK.getHeight() * 0.80;
							break;	
						default:
							pageWidth  = PageSize.LETTER.getWidth()  * 0.85;
							pageHeight = PageSize.LETTER.getHeight() * 0.80;
							break;
					}
					break;
				case "Landscape":
					switch (pageSize)
					{
						case "Letter":
							pageWidth  = PageSize.LETTER.getHeight()  * 0.85;
							pageHeight = PageSize.LETTER.getWidth() * 0.80;
							break;
						case "Note":
							pageWidth  = PageSize.NOTE.getHeight()  * 0.85;
							pageHeight = PageSize.NOTE.getWidth() * 0.80;
							break;
						case "Legal":
							pageWidth  = PageSize.LEGAL.getHeight()  * 0.85;
							pageHeight = PageSize.LEGAL.getWidth() * 0.80;
							break;
						case "Tabloid":
							pageWidth  = PageSize.TABLOID.getHeight()  * 0.85;
							pageHeight = PageSize.TABLOID.getWidth() * 0.80;
							break;
						case "Executive":
							pageWidth  = PageSize.EXECUTIVE.getHeight()  * 0.85;
							pageHeight = PageSize.EXECUTIVE.getWidth() * 0.80;
							break;
						case "Postcard":
							pageWidth  = PageSize.POSTCARD.getHeight()  * 0.85;
							pageHeight = PageSize.POSTCARD.getWidth() * 0.80;
							break;
						case "A0":
							pageWidth  = PageSize.A0.getHeight()  * 0.85;
							pageHeight = PageSize.A0.getWidth() * 0.80;
							break;
						case "A1":
							pageWidth  = PageSize.A1.getHeight()  * 0.85;
							pageHeight = PageSize.A1.getWidth() * 0.80;
							break;
						case "A2":
							pageWidth  = PageSize.A2.getHeight()  * 0.85;
							pageHeight = PageSize.A2.getWidth() * 0.80;
							break;
						case "A3":
							pageWidth  = PageSize.A3.getHeight()  * 0.85;
							pageHeight = PageSize.A3.getWidth() * 0.80;
							break;
						case "A4":
							pageWidth  = PageSize.A4.getHeight()  * 0.85;
							pageHeight = PageSize.A4.getWidth() * 0.80;
							break;
						case "A5":
							pageWidth  = PageSize.A5.getHeight()  * 0.85;
							pageHeight = PageSize.A5.getWidth() * 0.80;
							break;
						case "A6":
							pageWidth  = PageSize.A6.getHeight()  * 0.85;
							pageHeight = PageSize.A6.getWidth() * 0.80;
							break;
						case "A7":
							pageWidth  = PageSize.A7.getHeight()  * 0.85;
							pageHeight = PageSize.A7.getWidth() * 0.80;
							break;
						case "A8":
							pageWidth  = PageSize.A8.getHeight()  * 0.85;
							pageHeight = PageSize.A8.getWidth() * 0.80;
							break;
						case "A9":
							pageWidth  = PageSize.A9.getHeight()  * 0.85;
							pageHeight = PageSize.A9.getWidth() * 0.80;
							break;
						case "A10":
							pageWidth  = PageSize.A10.getHeight()  * 0.85;
							pageHeight = PageSize.A10.getWidth() * 0.80;
							break;
						case "B0":
							pageWidth  = PageSize.B0.getHeight()  * 0.85;
							pageHeight = PageSize.B0.getWidth() * 0.80;
							break;
						case "B1":
							pageWidth  = PageSize.B1.getHeight()  * 0.85;
							pageHeight = PageSize.B1.getWidth() * 0.80;
							break;
						case "B2":
							pageWidth  = PageSize.B2.getHeight()  * 0.85;
							pageHeight = PageSize.B2.getWidth() * 0.80;
							break;
						case "B3":
							pageWidth  = PageSize.B3.getHeight()  * 0.85;
							pageHeight = PageSize.B3.getWidth() * 0.80;
							break;
						case "B4":
							pageWidth  = PageSize.B4.getHeight()  * 0.85;
							pageHeight = PageSize.B4.getWidth() * 0.80;
							break;
						case "B5":
							pageWidth  = PageSize.B5.getHeight()  * 0.85;
							pageHeight = PageSize.B5.getWidth() * 0.80;
							break;
						case "B6":
							pageWidth  = PageSize.B6.getHeight()  * 0.85;
							pageHeight = PageSize.B6.getWidth() * 0.80;
							break;
						case "B7":
							pageWidth  = PageSize.B7.getHeight()  * 0.85;
							pageHeight = PageSize.B7.getWidth() * 0.80;
							break;
						case "B8":
							pageWidth  = PageSize.B8.getHeight()  * 0.85;
							pageHeight = PageSize.B8.getWidth() * 0.80;
							break;
						case "B9":
							pageWidth  = PageSize.B9.getHeight()  * 0.85;
							pageHeight = PageSize.B9.getWidth() * 0.80;
							break;
						case "B10":
							pageWidth  = PageSize.B10.getHeight()  * 0.85;
							pageHeight = PageSize.B10.getWidth() * 0.80;
							break;
						case "ArchE":
							pageWidth  = PageSize.ARCH_E.getHeight()  * 0.85;
							pageHeight = PageSize.ARCH_E.getWidth() * 0.80;
							break;
						case "ArchD":
							pageWidth  = PageSize.ARCH_D.getHeight()  * 0.85;
							pageHeight = PageSize.ARCH_D.getWidth() * 0.80;
							break;
						case "ArchC":
							pageWidth  = PageSize.ARCH_C.getHeight()  * 0.85;
							pageHeight = PageSize.ARCH_C.getWidth() * 0.80;
							break;
						case "ArchB":
							pageWidth  = PageSize.ARCH_B.getHeight()  * 0.85;
							pageHeight = PageSize.ARCH_B.getWidth() * 0.80;
							break;
						case "ArchA":
							pageWidth  = PageSize.ARCH_A.getHeight()  * 0.85;
							pageHeight = PageSize.ARCH_A.getWidth() * 0.80;
							break;
						case "FLSA":
							pageWidth  = PageSize.FLSA.getHeight()  * 0.85;
							pageHeight = PageSize.FLSA.getWidth() * 0.80;
							break;
						case "FLSE":
							pageWidth  = PageSize.FLSE.getHeight()  * 0.85;
							pageHeight = PageSize.FLSE.getWidth() * 0.80;
							break;
						case "HalfLetter":
							pageWidth  = PageSize.HALFLETTER.getHeight()  * 0.85;
							pageHeight = PageSize.HALFLETTER.getWidth() * 0.80;
							break;
						case "Ledger":
							pageWidth  = PageSize._11X17.getHeight()  * 0.85;
							pageHeight = PageSize._11X17.getWidth() * 0.80;
							break;
						case "ID1":
							pageWidth  = PageSize.ID_1.getHeight()  * 0.85;
							pageHeight = PageSize.ID_1.getWidth() * 0.80;
							break;
						case "ID2":
							pageWidth  = PageSize.ID_2.getHeight()  * 0.85;
							pageHeight = PageSize.ID_2.getWidth() * 0.80;
							break;
						case "ID3":
							pageWidth  = PageSize.ID_3.getHeight()  * 0.85;
							pageHeight = PageSize.ID_3.getWidth() * 0.80;
							break;
						case "CrownQuarto":
							pageWidth  = PageSize.CROWN_QUARTO.getHeight()  * 0.85;
							pageHeight = PageSize.CROWN_QUARTO.getWidth() * 0.80;
							break;
						case "LargeCrownQuarto":
							pageWidth  = PageSize.LARGE_CROWN_QUARTO.getHeight()  * 0.85;
							pageHeight = PageSize.LARGE_CROWN_QUARTO.getWidth() * 0.80;
							break;
						case "DemyQuarto":
							pageWidth  = PageSize.DEMY_QUARTO.getHeight()  * 0.85;
							pageHeight = PageSize.DEMY_QUARTO.getWidth() * 0.80;
							break;
						case "RoyalQuarto":
							pageWidth  = PageSize.ROYAL_QUARTO.getHeight()  * 0.85;
							pageHeight = PageSize.ROYAL_QUARTO.getWidth() * 0.80;
							break;
						case "CrownOctavo":
							pageWidth  = PageSize.CROWN_OCTAVO.getHeight()  * 0.85;
							pageHeight = PageSize.CROWN_OCTAVO.getWidth() * 0.80;
							break;
						case "LargeCrownOctavo":
							pageWidth  = PageSize.LARGE_CROWN_OCTAVO.getHeight()  * 0.85;
							pageHeight = PageSize.LARGE_CROWN_OCTAVO.getWidth() * 0.80;
							break;
						case "DemyOctavo":
							pageWidth  = PageSize.DEMY_OCTAVO.getHeight()  * 0.85;
							pageHeight = PageSize.DEMY_OCTAVO.getWidth() * 0.80;
							break;
						case "RoyalOctavo":
							pageWidth  = PageSize.ROYAL_OCTAVO.getHeight()  * 0.85;
							pageHeight = PageSize.ROYAL_OCTAVO.getWidth() * 0.80;
							break;
						case "SmallPaperback":
							pageWidth  = PageSize.SMALL_PAPERBACK.getHeight()  * 0.85;
							pageHeight = PageSize.SMALL_PAPERBACK.getWidth() * 0.80;
							break;
						case "PenguinSmallPaperback":
							pageWidth  = PageSize.PENGUIN_SMALL_PAPERBACK.getHeight()  * 0.85;
							pageHeight = PageSize.PENGUIN_SMALL_PAPERBACK.getWidth() * 0.80;
							break;
						case "PenguinLargePaperback":
							pageWidth  = PageSize.PENGUIN_LARGE_PAPERBACK.getHeight()  * 0.85;
							pageHeight = PageSize.PENGUIN_LARGE_PAPERBACK.getWidth() * 0.80;
							break;	
						default:
							pageWidth  = PageSize.LETTER.getHeight()  * 0.85;
							pageHeight = PageSize.LETTER.getWidth() * 0.80;
							break;
					}
					break;
				default:
					pageWidth  = PageSize.LETTER.getWidth()  * 0.85;
					pageHeight = PageSize.LETTER.getHeight() * 0.80;
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
