package com.OpenNAC.openauth.Services;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class RssReader extends AsyncTask<Void,Void,Void> {
    Context context;
    ProgressDialog progressDialog;
    String address;
    URL url;
    ArrayList<FeedItem>feedItems;
    RecyclerView recyclerView;
    public RssReader(Context context, RecyclerView recyclerView, String address){
        this.context = context;
        this.recyclerView = recyclerView;
        this.address = address;
        progressDialog = new ProgressDialog(context);

    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ProcessXml(GetData());
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
        MyAdapter adapter = new MyAdapter(context,feedItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    public void ProcessXml(Document document){
        if(document != null) {
            feedItems=new ArrayList<>();
            Element root = document.getDocumentElement();
            Node channel = root.getChildNodes().item(1);
            NodeList items = channel.getChildNodes();
            for (int i = 0;i<items.getLength();i++){
                Node currentChild = items.item(i);
                if(currentChild.getNodeName().equalsIgnoreCase("item")){
                    FeedItem item = new FeedItem();
                    NodeList itemChilds = currentChild.getChildNodes();
                    for (int j = 0; j<itemChilds.getLength();j++){
                        Node current = itemChilds.item(j);
                        if(current.getNodeName().equalsIgnoreCase("title")){
                            item.setTitle(current.getTextContent());
                        }
                        else if(current.getNodeName().equalsIgnoreCase("description")){
                            String desc = current.getTextContent();
                            String desc_after_dash = desc.substring(desc.indexOf("/>")+2, desc.indexOf("[&#8230;]"));
                            desc_after_dash = desc_after_dash.replaceAll("<p>","").
                                    replaceAll("</p>","").
                                    replaceAll("p>","").
                                    replaceAll("&#8217;", "'");
                            item.setDescription(desc_after_dash+"...");
                            if(desc.contains("src")){
                                String imageUrl = desc.substring(desc.indexOf("src")+5, desc.indexOf("png")+3);
                                //System.out.println("imageURL is: " + imageUrl.substring(imageUrl.indexOf("src")));
                                item.setThumbnailUrl(imageUrl);
                            }
                            else{
                                String imageUrl = "null";
                                item.setThumbnailUrl(imageUrl);
                            }

                        }
                        else if(current.getNodeName().equalsIgnoreCase("pubDate")){
                            item.setPubDate(current.getTextContent());
                        }
                        else if(current.getNodeName().equalsIgnoreCase("link")){
                            item.setLink(current.getTextContent());
                        }
                    }
                    feedItems.add(item);
                    Log.d("itemTitle", item.getTitle());
                    Log.d("itemLink", item.getLink());
                    Log.d("itemThumbnailUrl", item.getThumbnailUrl());
                    Log.d("itemDescription", item.getDescription());
                    Log.d("itemPubDate", item.getPubDate());
                }
            }
        }
    }
    public Document GetData(){
        try {
            url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=builderFactory.newDocumentBuilder();
            Document xmlDoc;
            xmlDoc = builder.parse(inputStream);
            return xmlDoc;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }
}
