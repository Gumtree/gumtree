import nblog
import datetime
import base64
import traceback

def test_append_plt_image() :
    try:
        import matplotlib.pyplot as plt
        # create a plot with matplotlib
        fig, ax = plt.subplots( nrows=1, ncols=1 ) 
        ax.plot([0,1,2], [10,20,3])
        # upload the plot to the E-Notebook as image
        resp = nblog.append_plt_image(plt, name = "test plt image", footer = "matplotlib image")
        print(resp)
    except Exception as e:
        print(e)
        traceback.format_exc()
    plt.close(fig)

def test_append_base64_image():
    try:
        # load an image file from a local path
        filename = "C:/Temp/test.jpg"
        with open(filename, "rb") as image2string:
            # encode the image into base64 string
            base64_image = base64.b64encode(image2string.read()).decode("utf-8").replace("\n", "") 
            # upload the base64 image string to the E-Notebook database
            resp = nblog.append_base64_image(base64_image, name = "test base64 image", footer = "image load from file: " + filename)
            print(resp)
    except Exception as e:
        print(e)
        traceback.format_exc()
        
def test_append_table_html():
    try:
        from tabulate import tabulate
        # create a simple 2D list formed as table rows.
        table_list = [['Motor Position','Value','Error'],
                      [0, 10, 3.2],
                      [1, 20, 4.5], 
                      [2, 3, 1.7]]
        # convert the list to html table string.
        table_text = tabulate(table_list, tablefmt = 'html')
        
        # upload the html table string to the E-Notebook database
        resp = nblog.append_table_entry(table_text, name = "simple HTML table")
        print(resp)
    except Exception as e:
        print(e)
        traceback.format_exc()
    
def test_append_time_stamp():
    try:
        # create a random text and upload it to the E-Notebook database
        resp = nblog.append_text_entry(datetime.datetime.now().strftime("%Y-%m-%dT%H_%M_%S.%f"), name = "test1")
        print(resp)
    except Exception as e:
        print(e)
        traceback.format_exc()
    
def test_suite():
    test_append_plt_image()
    test_append_base64_image()
    test_append_table_html()
    test_append_time_stamp()
    print('done')

if __name__ == '__main__':
    test_suite()