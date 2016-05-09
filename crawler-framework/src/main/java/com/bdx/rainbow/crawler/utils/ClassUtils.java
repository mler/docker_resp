/**
 * 
 */
package com.bdx.rainbow.crawler.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mler
 *
 */
public class ClassUtils {
	
	private static Logger logger = LoggerFactory.getLogger(ClassUtils.class);
	
	/**
	 * 获得包下面的所有的class
	 * 
	 * @param pack
	 *            package完整名称
	 * @return List包含所有class的实例
	 */
	public static Set<Class<?>> getClasssFromPackage(String pack) {
		Set<Class<?>> clazzs = new HashSet<Class<?>>();

		// 是否循环搜索子包
		boolean recursive = true;

		// 包名字
		String packageName = pack;
		// 包名对应的路径名称
		String packageDirName = packageName.replace('.', '/');

		Enumeration<URL> dirs;

		try {
			dirs = Thread.currentThread().getContextClassLoader()
					.getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();

				String protocol = url.getProtocol();

				if ("file".equals(protocol)) {
					logger.debug("file类型的扫描");
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					findClassInPackageByFile(packageName, filePath, recursive,
							clazzs);
				} else if ("jar".equals(protocol)) {
					logger.debug("jar类型的扫描");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return clazzs;
	}

	/**
	 * 在package对应的路径下找到所有的class
	 * 
	 * @param packageName
	 *            package名称
	 * @param filePath
	 *            package对应的路径
	 * @param recursive
	 *            是否查找子package
	 * @param clazzs
	 *            找到class以后存放的集合
	 */
	public static void findClassInPackageByFile(String packageName,
			String filePath, final boolean recursive, Set<Class<?>> clazzs) {
		File dir = new File(filePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		// 在给定的目录下找到所有的文件，并且进行条件过滤
		File[] dirFiles = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				boolean acceptDir = recursive && file.isDirectory();// 接受dir目录
				boolean acceptClass = file.getName().endsWith("class");// 接受class文件
				return acceptDir || acceptClass;
			}
		});

		for (File file : dirFiles) {
			if (file.isDirectory()) {
				findClassInPackageByFile(packageName + "." + file.getName(),
						file.getAbsolutePath(), recursive, clazzs);
			} else {
				String className = file.getName().substring(0,
						file.getName().length() - 6);
				try {
					clazzs.add(Thread.currentThread().getContextClassLoader()
							.loadClass(packageName + "." + className));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从jar文件中读取指定目录下面的所有的class文件
	 * 
	 * @param jarPaht
	 *            jar文件存放的位置
	 * @param filePaht
	 *            指定的文件目录
	 * @return 所有的的class的对象
	 */
	@SuppressWarnings("resource")
	public Set<Class<?>> getClasssFromJarFile(String jarPaht, String filePaht) {
		Set<Class<?>> clazzs = new HashSet<Class<?>>();

		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarPaht);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		List<JarEntry> jarEntryList = new ArrayList<JarEntry>();

		Enumeration<JarEntry> ee = jarFile.entries();
		while (ee.hasMoreElements()) {
			JarEntry entry = (JarEntry) ee.nextElement();
			// 过滤我们出满足我们需求的东西
			if (entry.getName().startsWith(filePaht)
					&& entry.getName().endsWith(".class")) {
				jarEntryList.add(entry);
			}
		}
		for (JarEntry entry : jarEntryList) {
			String className = entry.getName().replace('/', '.');
			className = className.substring(0, className.length() - 6);

			try {
				clazzs.add(Thread.currentThread().getContextClassLoader()
						.loadClass(className));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return clazzs;
	}

	/**
	 * 通过流将jar中的一个文件的内容输出
	 * 
	 * @param jarPaht
	 *            jar文件存放的位置
	 * @param filePaht
	 *            指定的文件目录
	 */
	public static void getStream(String jarPaht, String filePaht) {
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarPaht);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Enumeration<JarEntry> ee = jarFile.entries();

		List<JarEntry> jarEntryList = new ArrayList<JarEntry>();
		while (ee.hasMoreElements()) {
			JarEntry entry = (JarEntry) ee.nextElement();
			// 过滤我们出满足我们需求的东西，这里的fileName是指向一个具体的文件的对象的完整包路径，比如com/mypackage/test.txt
			if (entry.getName().startsWith(filePaht)) {
				jarEntryList.add(entry);
			}
		}
		try {
			InputStream in = jarFile.getInputStream(jarEntryList.get(0));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String s = "";

			while ((s = br.readLine()) != null) {
				logger.debug(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取泛型的具体类型
	 * @param actionClass
	 * @return
	 */
	public static Class<?>[] getParameterizedType(Class<?> actionClass)
	{
		Type genType = actionClass.getGenericSuperclass();

		if (!(genType instanceof ParameterizedType)) {
			logger.debug(actionClass.getSimpleName() + "'s superclass not ParameterizedType");
			return null;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if (0 >= params.length) {
			logger.debug("Index: " + 0 + ", Size of " + actionClass.getSimpleName() + "'s Parameterized Type: "
					+ params.length);
		}
		
		Class<?>[] clazzes = new Class<?>[params.length];
		
		for(int i=0;i<params.length;i++)
		{
			if (params[i] instanceof Class) {
				logger.debug(actionClass.getSimpleName() + " not set the actual class on superclass generic parameter");
				clazzes[i] = (Class<?>)params[i];
			}
		}
		
		return clazzes;
	}
	
	
	public static void main(String[] args) {
		Set<Class<?>> clazzs = ClassUtils.getClasssFromPackage("com");//
		for (Class<?> clazz : clazzs) {
//			logger.debug(clazz.getName());
			System.out.println(clazz.getName());
		}

		// clazzs = classUtil.getClasssFromJarFile("Jar文件的路径", "Jar文件里面的包路径");
		// for (Class clazz : clazzs) {
		// logger.debug(clazz.getName());
		// }
		//
		// classUtil.getStream("Jar文件的路径", "Jar文件总的一个具体文件的路径");
	}

}
