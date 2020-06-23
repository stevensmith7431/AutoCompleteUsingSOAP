package com.example.autocompletesoap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class WebService {

    public static String WebServiceCall(String[] a, String[] b, String Method, String Namespace, String URL) {
        String responsestring="";
        try
        {
            SoapObject request = new SoapObject(Namespace, Method);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            if(a!=null) {
                for (int i = 0; i <= a.length - 1; i++) {
                    request.addProperty(a[i], b[i]);
                }
            }
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
            try {
                androidHttpTransport.call(Namespace + Method,
                        envelope);
            } catch (IOException | XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            SoapPrimitive response = null;
            try {
                response = (SoapPrimitive) envelope.getResponse();
                responsestring = response.toString();
            } catch (SoapFault e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "false " + e.toString();
            }
            return responsestring;
        }
        catch(Exception e)
        {
            return "false " + e.toString();       //	return false;
        }
    }
}