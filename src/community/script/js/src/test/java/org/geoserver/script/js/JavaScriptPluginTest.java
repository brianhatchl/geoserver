package org.geoserver.script.js;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.geoserver.script.ScriptIntTestSupport;
import org.geoserver.script.ScriptManager;
import org.geoserver.script.ScriptPlugin;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

public class JavaScriptPluginTest extends ScriptIntTestSupport {
    
    JavaScriptPlugin getPlugin() {
        JavaScriptPlugin plugin = null;
        List<ScriptPlugin> plugins = getScriptManager().getPlugins();
        for (ScriptPlugin candidate : plugins) {
            if (candidate instanceof JavaScriptPlugin) {
                plugin = (JavaScriptPlugin) candidate;
                break;
            }
        }
        return plugin;
    }

    /**
     * Test method for {@link org.geoserver.script.js.JavaScriptPlugin#getModulePaths()}.
     * @throws URISyntaxException 
     */
    public void testGetModulePaths() throws URISyntaxException {
        JavaScriptPlugin plugin = getPlugin();
        List<String> paths = plugin.getModulePaths();
        assertTrue("got some paths", paths.size() > 0);
        for (String path : paths) {
            URI uri = new URI(path);
            assertTrue("absolute URI", uri.isAbsolute());
            String scheme = uri.getScheme();
            if (scheme.equals("file")) {
                File file = new File(uri.getPath());
                assertTrue("path is directory", file.isDirectory());
                assertTrue("directory exists", file.exists());
            }
        }
    }

    /**
     * Test method for {@link org.geoserver.geoscript.javascript.JavaScriptModules#require()}.
     * @throws ScriptException 
     */
    public void testRequireGeoScript() throws ScriptException {
        ScriptManager scriptMgr = getScriptManager();
        ScriptEngine engine = scriptMgr.createNewEngine("js");
        engine.eval("var gs = require('geoscript')");
        Object exportsObj = engine.get("gs");
        assertTrue(exportsObj instanceof Scriptable);
        Scriptable exports = (Scriptable) exportsObj;
        Object geomObj = exports.get("geom", exports);
        assertTrue("geom in exports", geomObj instanceof Scriptable);
        Object projObj = exports.get("proj", exports);
        assertTrue("proj in exports", projObj instanceof Scriptable);
    }

    /**
     * Test method for {@link org.geoserver.geoscript.javascript.JavaScriptModules#require()}.
     * @throws ScriptException 
     */
    public void testRequireGeoServer() throws ScriptException {
        ScriptManager scriptMgr = getScriptManager();
        ScriptEngine engine = scriptMgr.createNewEngine("js");
        engine.eval("var gs = require('geoserver')");
        Object exportsObj = engine.get("gs");
        assertTrue(exportsObj instanceof Scriptable);
        Scriptable exports = (Scriptable) exportsObj;
        Object catalogObj = exports.get("catalog", exports);
        assertTrue("catalog in exports", catalogObj instanceof Scriptable);
    }

    /**
     * Test for catalog access through the geoserver.js module.
     * @throws ScriptException 
     */
    public void testGeoServerCatalogNamespaces() throws ScriptException {
        
        ScriptEngine engine = getScriptManager().createNewEngine("js");

        // get list of namespaces in catalog
        Object result = engine.eval("require('geoserver/catalog').namespaces");
        assertTrue(result instanceof NativeArray);
        NativeArray array = (NativeArray) result;
        assertEquals("correct number of namespaces", 5, array.getLength());
        Scriptable obj = (Scriptable) array.get(0);
        assertEquals("first namespace alias", "cite", obj.get("alias", obj));
        assertEquals("first namespace uri", "http://www.opengis.net/cite", obj.get("uri", obj));
    }

    /**
     * Test for catalog access through the geoserver.js module.
     * @throws ScriptException 
     */
    public void testGeoServerCatalogGetVectorLayer() throws ScriptException {
        
        ScriptEngine engine = getScriptManager().createNewEngine("js");
        
        String script = 
            "var catalog = require('geoserver/catalog');" + 
            "var Layer = require('geoscript/layer').Layer;" +
            "var buildings = catalog.getVectorLayer('cite:Buildings');" +
            "buildings instanceof Layer";

        // get a layer from the catalog
        Object result = engine.eval(script);
        assertTrue(result instanceof Boolean);
        assertEquals("got layer", result, true);
    }

    
}
