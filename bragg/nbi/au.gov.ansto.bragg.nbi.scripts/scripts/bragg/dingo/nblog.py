import datetime
import io
import os
import base64
import requests

URL_DB_ADDRESS = 'http://gumtree.nbi.ansto.gov.au'
URL_NB_DB_APPEND = URL_DB_ADDRESS + ':61251/user/db/append'
# URL_NB_DB_APPEND = 'http://localhost:63351/user/db/append'

def append_plt_image(plt, name = "", footer = "") :
    ''' Append matplotlib image to the E-Notebook database by passing the pyplot handle. 
        For details of how to create plot with pyplot handle, check this website: https://matplotlib.org/stable/api/pyplot_summary.html
        
        Parameters:
            plt: matplotlib.pyplot handle. e.g.: import matplotlib.pyplot as plt
            
            name: optional; name of the plot, used for searching purpose.
            
            footer: optional; used for footer labelling
            
        Returns: None
        
    '''
    base64_image = io.BytesIO()
    plt.savefig(base64_image, format='png')
    base64_image = base64.b64encode(base64_image.getvalue()).decode("utf-8").replace("\n", "")
#     base64_image = '<img align="left" src="data:image/png;base64,%s">' % s
    return append_base64_image(base64_image, name, footer);

def append_base64_image(base64_image, name = "", footer = "") : 
    ''' Append base64 encoded image to the E-Notebook database.  
        For details of how to encode image into base64 ASCII characters, check this website: https://www.askpython.com/python/examples/encoding-image-with-base64
        
        Example:
            import base64
            with open("file_with_path", "rb") as image2string: 
                base64_image = base64.b64encode(image2string.read()).decode("utf-8").replace("\n", "") 
                append_base64_image(base64_image, "image name", "image footer")
        
        Parameters:
            base64_image: image series encoded in base64 ASCII characters.
            
            name: name of the plot, used for searching purpose.
            
            footer: optional, used for footer labelling
            
        Returns: None
        
    '''
    html = "<img src=\"data:image/png;base64," + base64_image + "\" alt=\"" + name + "\">";
    if footer != None and len(footer) > 0 :
        html += "<br><span class=\"class_span_tablefoot\">" + footer + "</span><br>";            
    class_name = "class_db_image";
    return _append_class_entry(class_name, name, html);

def append_text_entry(text, name = ""): 
    ''' Append plain text block to the E-Notebook database.  
        
        Parameters:
            text: string of ASCII characters.
            
            name: optional; name of the plot, used for searching purpose.
            
        Returns: None
        
    '''
    class_name = "class_db_text";
    return _append_class_entry(name, class_name, text);

def append_table_entry(table_text, name = "") : 
    ''' Append HTML table block to the E-Notebook database.  
        For details about how to create HTML in Python, check this website: https://pypi.org/project/tabulate/
        
        Example:
            from tabulate import tabulate
            table_list = [['Motor Position','Value','Error'],
                          [0, 10, 3.2],
                          [1, 20, 4.5], 
                          [2, 3, 1.7]]
            table_text = tabulate(table_list, tablefmt = 'html')
            nblog.append_table_entry(table_text, name = "simple HTML table")
            
        Parameters:
            table_text: HTML table series in string of ASCII characters.
            
            name: optional; name of the plot, used for searching purpose.
            
        Returns: None
        
    '''
    class_name = "class_db_table";
    return _append_class_entry(name, class_name, table_text);

def _wrap_db_object(key, class_name, html):
    output = '<div class="' + class_name + ' class_db_object" id="' + key + '">'
    output += html
    output += '</div>'
    data = {}
    data["key"] = key
    data["html"] = output.encode('ascii', 'ignore')
    return data;
    
def _append_class_entry(name, class_name, text) :
    key = name + "_" + datetime.datetime.now().strftime("%Y-%m-%dT%H_%M_%S.%f")
    return _append_html_entry(_wrap_db_object(key, class_name, text));        

def _append_html_entry(dict_data) :
#     data = parse.urlencode(dict_data).encode()
    session = requests.Session()
    session.trust_env = False
    resp = requests.post(URL_NB_DB_APPEND, data = dict_data)
#     req =  request.Request(URL_NB_DB_APPEND, data=data) # this will make the method "POST"
#     resp = request.urlopen(req)
    return resp

