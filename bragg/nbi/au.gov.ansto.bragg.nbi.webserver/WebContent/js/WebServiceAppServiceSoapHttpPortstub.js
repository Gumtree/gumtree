function firstChildElement (node) {
    if (!node)        return null;    var child = node.firstChild;
    while (child) {
        if (child.nodeType == 1)
            return child;
        child = child.nextSibling;
    }
    return null;
}

function nextSiblingElement (node) {
    if (!node)        return null;    var sibling = node.nextSibling;
    while (sibling) {
        if (sibling.nodeType == 1)
            return sibling;
        sibling = sibling.nextSibling;
    }
    return null;
}

function getText (node) {
    if (!node)        return null;    var text = '';
    var child = node.firstChild;
    while (child) {
        if (child.nodeType == 3) {
            text = text + child.nodeValue;
        }
        child = child.nextSibling;
    }
    return text;
}

function invokeSync (url, xmlDoc) {
    var req = null;    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }

    if (req) {
        req.open("POST", url, false);
        req.setRequestHeader("Content-Type", "text/xml");
        req.send(xmlDoc);
        return req.responseXML;
    }
}

function invokeAsync (url, xmlDoc, callback) {
    var req = null;
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }
    
    if (req) {
        req.onreadystatechange = function () {
            if (req.readyState == 4) {
                if (req.status == 200) {
                    callback(req.responseXML);
                } 
            }
        }
        req.open("POST", url, true);
        req.setRequestHeader("Content-Type", "text/xml");
        req.send(xmlDoc);
    }
}

function createNewDocument () {
    var xmlDoc = null;
    if (document.implementation && document.implementation.createDocument) {
        xmlDoc = document.implementation.createDocument("", "", null);
    } else if (window.ActiveXObject){
        xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
    }
    return xmlDoc;
}

function createElementNS (xmlDoc, namespace, localName) {
    var element = null;
    if (typeof xmlDoc.createElementNS != 'undefined') {
        element = xmlDoc.createElementNS(namespace, localName);
    }
    else if (typeof xmlDoc.createNode != 'undefined') {
        if (namespace) {
            element = xmlDoc.createNode(1, localName, namespace);
        } else {
            element = xmlDoc.createElement(localName);
        }
    }
    return element;
}

function localName (element) {
    if (element.localName)
        return element.localName;
    else
        return element.baseName;
}



function WebServiceAppServiceSoapHttpPort_getCoInvestigators(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getCoInvestigatorsElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj = [];
    if (resultEl) {
        for (var achild=firstChildElement(resultEl.parentNode); achild; achild = nextSiblingElement(achild)) {
            resultObj[resultObj.length] = getText(achild);
        }
    }
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getCoInvestigatorsAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getCoInvestigatorsElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj = [];
    if (resultEl) {
        for (var achild=firstChildElement(resultEl.parentNode); achild; achild = nextSiblingElement(achild)) {
            resultObj[resultObj.length] = getText(achild);
        }
    }
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getProposalTitle(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalTitleElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getProposalTitleAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalTitleElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getPrincipalInvestigatorID(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getPrincipalInvestigatorIDElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getPrincipalInvestigatorIDAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getPrincipalInvestigatorIDElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getBookingDetails(_proposalId, _instrumentId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getBookingDetailsElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'instrumentId');
    paramEl.appendChild(xmlDoc.createTextNode(_instrumentId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj = [];
    if (resultEl) {
        for (var achild=firstChildElement(resultEl.parentNode); achild; achild = nextSiblingElement(achild)) {
            resultObj[resultObj.length] = WebServiceAppServiceSoapHttpPort_deserialize_BookingInfo('result', resultEl.parentNode);
        }
    }
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getBookingDetailsAsync(_proposalId, _instrumentId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getBookingDetailsElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'instrumentId');
    paramEl.appendChild(xmlDoc.createTextNode(_instrumentId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj = [];
    if (resultEl) {
        for (var achild=firstChildElement(resultEl.parentNode); achild; achild = nextSiblingElement(achild)) {
            resultObj[resultObj.length] = WebServiceAppServiceSoapHttpPort_deserialize_BookingInfo('result', resultEl.parentNode);
        }
    }
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getScientificArea(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getScientificAreaElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getScientificAreaAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getScientificAreaElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getReactorPower() {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getReactorPowerElement');
    body.appendChild(parameterParent);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    if (resultEl)
        resultObj = WebServiceAppServiceSoapHttpPort_deserialize_ValueAtTime(resultEl);
    else
        resultObj = null;
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getReactorPowerAsync(callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getReactorPowerElement');
    body.appendChild(parameterParent);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    if (resultEl)
        resultObj = WebServiceAppServiceSoapHttpPort_deserialize_ValueAtTime(resultEl);
    else
        resultObj = null;
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getReactorDisplay() {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getReactorDisplayElement');
    body.appendChild(parameterParent);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    if (resultEl)
        resultObj = WebServiceAppServiceSoapHttpPort_deserialize_ReactorDisplayInfo(resultEl);
    else
        resultObj = null;
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getReactorDisplayAsync(callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getReactorDisplayElement');
    body.appendChild(parameterParent);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    if (resultEl)
        resultObj = WebServiceAppServiceSoapHttpPort_deserialize_ReactorDisplayInfo(resultEl);
    else
        resultObj = null;
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getProposalTags(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalTagsElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj = [];
    if (resultEl) {
        for (var achild=firstChildElement(resultEl.parentNode); achild; achild = nextSiblingElement(achild)) {
            resultObj[resultObj.length] = getText(achild);
        }
    }
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getProposalTagsAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalTagsElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj = [];
    if (resultEl) {
        for (var achild=firstChildElement(resultEl.parentNode); achild; achild = nextSiblingElement(achild)) {
            resultObj[resultObj.length] = getText(achild);
        }
    }
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getBraggInfo() {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getBraggInfoElement');
    body.appendChild(parameterParent);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    if (resultEl)
        resultObj = WebServiceAppServiceSoapHttpPort_deserialize_AllDisplayInfo(resultEl);
    else
        resultObj = null;
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getBraggInfoAsync(callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getBraggInfoElement');
    body.appendChild(parameterParent);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    if (resultEl)
        resultObj = WebServiceAppServiceSoapHttpPort_deserialize_AllDisplayInfo(resultEl);
    else
        resultObj = null;
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getProposalText(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalTextElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getProposalTextAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalTextElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getProposalRound(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalRoundElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getProposalRoundAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalRoundElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_validateProposal(_proposalId, _email) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'validateProposalElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'email');
    paramEl.appendChild(xmlDoc.createTextNode(_email));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_validateProposalAsync(_proposalId, _email, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'validateProposalElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'email');
    paramEl.appendChild(xmlDoc.createTextNode(_email));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getProposalReferences(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalReferencesElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getProposalReferencesAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalReferencesElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getReactorSimpleDisplay() {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getReactorSimpleDisplayElement');
    body.appendChild(parameterParent);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getReactorSimpleDisplayAsync(callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getReactorSimpleDisplayElement');
    body.appendChild(parameterParent);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getProposalId(_proposalCode) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalIdElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalCode');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalCode));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getProposalIdAsync(_proposalCode, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalIdElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalCode');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalCode));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    resultObj =  getText(resultEl);
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_getProposalInfo(_proposalId) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalInfoElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var responseDoc = invokeSync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc);
    var resultObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    if (resultEl)
        resultObj = WebServiceAppServiceSoapHttpPort_deserialize_ExperimentDisplayInternal(resultEl);
    else
        resultObj = null;
    return resultObj;
}

function WebServiceAppServiceSoapHttpPort_getProposalInfoAsync(_proposalId, callback) {
    var xmlDoc = createNewDocument();
    var envelope = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Envelope');
    xmlDoc.appendChild(envelope);
    var body = createElementNS(xmlDoc, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body');
    envelope.appendChild(body);
    var parameterParent = body;
    parameterParent = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'getProposalInfoElement');
    body.appendChild(parameterParent);
    var paramEl = createElementNS(xmlDoc, 'http://au/gov/ansto/bragg/web/model/webService/server/webservice/WebServiceAppServer.wsdl', 'proposalId');
    paramEl.appendChild(xmlDoc.createTextNode(_proposalId));
    parameterParent.appendChild(paramEl);
    var resultsProcessor = function (responseDoc) {
    var resultsObj = null;
    body = firstChildElement(responseDoc.documentElement);
    if (localName(body) != 'Body') {
        body = nextSiblingElement(body);
    }

    var resultEl = firstChildElement(body);
    resultEl = firstChildElement(resultEl);
    if (resultEl)
        resultObj = WebServiceAppServiceSoapHttpPort_deserialize_ExperimentDisplayInternal(resultEl);
    else
        resultObj = null;
    callback(resultObj);
    }
    invokeAsync('http://neutron.ansto.gov.au/WebServices/WebServiceAppServiceSoapHttpPort', xmlDoc, resultsProcessor);
}

function WebServiceAppServiceSoapHttpPort_deserialize_BookingInfo(valueEl) {
    var resultsObject = {};
    for (var child=firstChildElement(valueEl); child; child = nextSiblingElement(child)) {
        if (localName(child) == 'groupId') {
            resultsObject.groupId = getText(child);
        }
        if (localName(child) == 'bookingEnd') {
            resultsObject.bookingEnd = getText(child);
        }
        if (localName(child) == 'equipmentId') {
            resultsObject.equipmentId = getText(child);
        }
        if (localName(child) == 'startTime') {
            resultsObject.startTime = getText(child);
        }
        if (localName(child) == 'visitNumber') {
            resultsObject.visitNumber = getText(child);
        }
        if (localName(child) == 'bookingStart') {
            resultsObject.bookingStart = getText(child);
        }
        if (localName(child) == 'endTime') {
            resultsObject.endTime = getText(child);
        }
    }
    return resultsObject;
}

function WebServiceAppServiceSoapHttpPort_deserialize_ValueAtTime(valueEl) {
    var resultsObject = {};
    for (var child=firstChildElement(valueEl); child; child = nextSiblingElement(child)) {
        if (localName(child) == 'value') {
            resultsObject.value = getText(child);
        }
        if (localName(child) == 'timeStamp') {
            resultsObject.timeStamp = getText(child);
        }
    }
    return resultsObject;
}

function WebServiceAppServiceSoapHttpPort_deserialize_ReactorDisplayInfo(valueEl) {
    var resultsObject = {};
    for (var child=firstChildElement(valueEl); child; child = nextSiblingElement(child)) {
        if (localName(child) == 'timeStamp') {
            resultsObject.timeStamp = getText(child);
        }
        if (localName(child) == 'cnsOutTemp') {
            resultsObject.cnsOutTemp = getText(child);
        }
        if (localName(child) == 'tg123Status') {
            resultsObject.tg123Status = getText(child);
        }
        if (localName(child) == 'hg2Status') {
            resultsObject.hg2Status = getText(child);
        }
        if (localName(child) == 'errorMsg') {
            resultsObject.errorMsg = getText(child);
        }
        if (localName(child) == 'tg4Status') {
            resultsObject.tg4Status = getText(child);
        }
        if (localName(child) == 'reactorPower') {
            resultsObject.reactorPower = getText(child);
        }
        if (localName(child) == 'cnsInTemp') {
            resultsObject.cnsInTemp = getText(child);
        }
        if (localName(child) == 'cnsTemp') {
            resultsObject.cnsTemp = getText(child);
        }
        if (localName(child) == 'cg4Status') {
            resultsObject.cg4Status = getText(child);
        }
        if (localName(child) == 'cg123Status') {
            resultsObject.cg123Status = getText(child);
        }
    }
    return resultsObject;
}

function WebServiceAppServiceSoapHttpPort_deserialize_AllDisplayInfo(valueEl) {
    var resultsObject = {};
    resultsObject.seminarInfo = [];
    resultsObject.experimentInfo = [];
    for (var child=firstChildElement(valueEl); child; child = nextSiblingElement(child)) {
        if (localName(child) == 'screenMessage') {
            resultsObject.screenMessage = getText(child);
        }
        if (localName(child) == 'timeStamp') {
            resultsObject.timeStamp = getText(child);
        }
        if (localName(child) == 'timeStr') {
            resultsObject.timeStr = getText(child);
        }
        if (localName(child) == 'seminarInfo') {
            resultsObject.seminarInfo[resultsObject.seminarInfo.length] = WebServiceAppServiceSoapHttpPort_deserialize_SeminarInfo(child);
        }
        if (localName(child) == 'reactorInfo') {
            resultsObject.reactorInfo = WebServiceAppServiceSoapHttpPort_deserialize_ReactorDisplayInfo(child);
        }
        if (localName(child) == 'errorMsg') {
            resultsObject.errorMsg = getText(child);
        }
        if (localName(child) == 'experimentInfo') {
            resultsObject.experimentInfo[resultsObject.experimentInfo.length] = WebServiceAppServiceSoapHttpPort_deserialize_ExperimentDisplay(child);
        }
        if (localName(child) == 'dateStr') {
            resultsObject.dateStr = getText(child);
        }
        if (localName(child) == 'newsBar') {
            resultsObject.newsBar = getText(child);
        }
    }
    return resultsObject;
}

function WebServiceAppServiceSoapHttpPort_deserialize_ExperimentDisplayInternal(valueEl) {
    var resultsObject = {};
    resultsObject.bookingArray = [];
    for (var child=firstChildElement(valueEl); child; child = nextSiblingElement(child)) {
        if (localName(child) == 'proposalCode') {
            resultsObject.proposalCode = getText(child);
        }
        if (localName(child) == 'principalSci') {
            resultsObject.principalSci = getText(child);
        }
        if (localName(child) == 'principalEmail') {
            resultsObject.principalEmail = getText(child);
        }
        if (localName(child) == 'otherEmail') {
            resultsObject.otherEmail = getText(child);
        }
        if (localName(child) == 'exptTitle') {
            resultsObject.exptTitle = getText(child);
        }
        if (localName(child) == 'text') {
            resultsObject.text = getText(child);
        }
        if (localName(child) == 'otherSci') {
            resultsObject.otherSci = getText(child);
        }
        if (localName(child) == 'bookingArray') {
            resultsObject.bookingArray[resultsObject.bookingArray.length] = WebServiceAppServiceSoapHttpPort_deserialize_BookingDisplayInternal(child);
        }
    }
    return resultsObject;
}

function WebServiceAppServiceSoapHttpPort_deserialize_SeminarInfo(valueEl) {
    var resultsObject = {};
    for (var child=firstChildElement(valueEl); child; child = nextSiblingElement(child)) {
        if (localName(child) == 'presenterInfo') {
            resultsObject.presenterInfo = getText(child);
        }
        if (localName(child) == 'type') {
            resultsObject.type = getText(child);
        }
        if (localName(child) == 'title') {
            resultsObject.title = getText(child);
        }
        if (localName(child) == 'timeStr') {
            resultsObject.timeStr = getText(child);
        }
        if (localName(child) == 'talkDate') {
            resultsObject.talkDate = getText(child);
        }
        if (localName(child) == 'host') {
            resultsObject.host = getText(child);
        }
        if (localName(child) == 'presenter') {
            resultsObject.presenter = getText(child);
        }
        if (localName(child) == 'location') {
            resultsObject.location = getText(child);
        }
        if (localName(child) == 'dateStr') {
            resultsObject.dateStr = getText(child);
        }
        if (localName(child) == 'imminent') {
            resultsObject.imminent = getText(child);
        }
        if (localName(child) == 'lengthStr') {
            resultsObject.lengthStr = getText(child);
        }
    }
    return resultsObject;
}

function WebServiceAppServiceSoapHttpPort_deserialize_ExperimentDisplay(valueEl) {
    var resultsObject = {};
    for (var child=firstChildElement(valueEl); child; child = nextSiblingElement(child)) {
        if (localName(child) == 'proposalCode') {
            resultsObject.proposalCode = getText(child);
        }
        if (localName(child) == 'principalSci') {
            resultsObject.principalSci = getText(child);
        }
        if (localName(child) == 'principalOrg') {
            resultsObject.principalOrg = getText(child);
        }
        if (localName(child) == 'exptTitle') {
            resultsObject.exptTitle = getText(child);
        }
        if (localName(child) == 'endDisplay') {
            resultsObject.endDisplay = getText(child);
        }
        if (localName(child) == 'otherSci') {
            resultsObject.otherSci = getText(child);
        }
        if (localName(child) == 'errorMsg') {
            resultsObject.errorMsg = getText(child);
        }
        if (localName(child) == 'startDisplay') {
            resultsObject.startDisplay = getText(child);
        }
        if (localName(child) == 'instrName') {
            resultsObject.instrName = getText(child);
        }
        if (localName(child) == 'localSci') {
            resultsObject.localSci = getText(child);
        }
    }
    return resultsObject;
}

function WebServiceAppServiceSoapHttpPort_deserialize_BookingDisplayInternal(valueEl) {
    var resultsObject = {};
    for (var child=firstChildElement(valueEl); child; child = nextSiblingElement(child)) {
        if (localName(child) == 'instrument') {
            resultsObject.instrument = getText(child);
        }
        if (localName(child) == 'startDate') {
            resultsObject.startDate = getText(child);
        }
        if (localName(child) == 'endDate') {
            resultsObject.endDate = getText(child);
        }
        if (localName(child) == 'localContact') {
            resultsObject.localContact = getText(child);
        }
    }
    return resultsObject;
}

