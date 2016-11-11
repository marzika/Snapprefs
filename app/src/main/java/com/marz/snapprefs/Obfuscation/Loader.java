package com.marz.snapprefs.Obfuscation;

/**
 * Created by Andre on 04/10/2016.
 */

public class Loader {

    /*public static void buildTestClass() throws IOException {
        DataHolder dh = new DataHolder();

        ConcurrentHashMap<String, DataHolder.MethodData> ml = new ConcurrentHashMap<>();

        String[] strList = new String[] { "param1", "param2"};
        DataHolder.MethodData md = new DataHolder.MethodData("testClass1", "testMethod1", strList);
        ml.put("testMethod1", md);

        strList = new String[] { "param1", "param2", "param3"};
        md = new DataHolder.MethodData("testClass1", "testMethod2", strList);
        ml.put("testMethod2", md);

        strList = new String[] { "param1"};
        md = new DataHolder.MethodData("testClass1", "testMethod3", strList);
        ml.put("testMethod3", md);

        dh.classMap.put("testClass", new DataHolder.ClassData("testClass1", ml));
        dh.classMap.put("testClass2", new DataHolder.ClassData("testClass2", ml));

        writeClasses(dh);
    }

    public static void writeClasses(DataHolder dataHolder ) throws IOException {
        File file = new File(Preferences.getSavePath() + "/TestFile.json");

        FileOutputStream out = new FileOutputStream(file);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");

        writer.beginArray();

        for(ClassData classData : dataHolder.classMap.values())
        {
            writer.beginObject();

            writer.name(classData.className);

            writeMethods(writer, classData);

            writer.endObject();
        }
        writer.endArray();

        writer.close();
    }

    public static void writeMethods(JsonWriter writer, DataHolder.ClassData classData) throws IOException {
        Log.d("snapprefs", "Writing methods");

        writer.name(classData);
        writer.beginObject();

        Log.d("snapprefs", "Writing methods DONE");
        for(DataHolder.MethodData md : classData.methodMap.values())
        {
            writer.name("M").value(md.methodName);

            writeParams(writer, md);
        }
        writer.endObject();
    }

    public static void writeParams(JsonWriter writer, DataHolder.MethodData md) throws IOException {
        writer.name("Params");
        writer.beginArray();
        for( String param : md.parameters)
            writer.value(param);
        writer.endArray();
    }


    public static void testParse() throws FileNotFoundException {
        FileInputStream in = new FileInputStream(new File(Preferences.getSavePath() + "/TestFile.json"));
        JsonReader reader = new JsonReader(new InputStreamReader(in));


    }*/
}
