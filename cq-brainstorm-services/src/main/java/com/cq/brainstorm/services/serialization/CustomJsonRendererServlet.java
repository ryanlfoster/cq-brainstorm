package com.cq.brainstorm.services.serialization;

import com.google.gson.stream.JsonWriter;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.HttpHeaders;
import org.apache.http.client.utils.DateUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component(
        label = "Samples - Custom JSON Renderer, handles all requests like *.custom.json",
        metatype = true, immediate = true
)
@Service(javax.servlet.Servlet.class)
@Properties({
        @Property(name = "sling.servlet.resourceTypes", value = "sling/servlet/default"),
        @Property(name = "sling.servlet.selectors", value = "custom", propertyPrivate = true),
        @Property(name = "sling.servlet.extensions", value = "json"),
        @Property(name = "sling.servlet.methods", value = "GET")
})
public class CustomJsonRendererServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");

        Date lastModified = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)); // fake value
        response.setHeader(HttpHeaders.LAST_MODIFIED, DateUtils.formatDate(lastModified));

        JsonWriter writer = new JsonWriter(response.getWriter());
        writer.beginObject()
                .name("producedBy").value(getClass().getSimpleName())
                .name("date").value(DateUtils.formatDate(new Date()))
                .name("path").value(request.getResource().getPath())
                .name("requiredProperty").value("requiredPropertyValue")
                .name("lastModified").value(DateUtils.formatDate(lastModified));
        writer.endObject().close();
    }
}
