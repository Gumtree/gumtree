package org.gumtree.flex
{
	import br.com.stimuli.loading.BulkLoader;
	
	import flash.display.Loader;
	import flash.events.Event;
	import flash.net.URLRequest;
	
	import mx.core.UIComponent;

	public class ImageView extends UIComponent
	{
		public var url:String;
		private var bulkloader:BulkLoader;

		public function ImageView()
		{
		}
		
		public function update():void
		{
			if (url == null)
			{
				return;
			}
			if (bulkloader == null)
			{
				// Create loader
				bulkloader = new BulkLoader(url);
				bulkloader.addEventListener(BulkLoader.COMPLETE, onComplete);
			}
			// Load image
			bulkloader.add(url, {"id":"image", "type":"image"});
			bulkloader.reload("image");
		}
		
		private function onComplete(event:Event):void
		{
			// Update UI
			addChild(bulkloader.getContent("image"));
		}
		
	}
	
}