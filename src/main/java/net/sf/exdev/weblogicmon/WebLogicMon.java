package net.sf.exdev.weblogicmon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class WebLogicMon {

	private MBeanServerConnection connection;
	private JMXConnector connector;
	private ObjectName service;
	private String hostame;
	private String port;

	public ObjectName getService() {
		return service;
	}

	public void setService(ObjectName service) {
		this.service = service;
	}

	public WebLogicMon(String address, String port, String user, String password) throws IOException {

		String protocol = "t3";
		String resource = "/jndi/";
		String uri = "weblogic.management.mbeanservers.domainruntime";

		Map<String, String> map = new Hashtable<String, String>();
		map.put("java.naming.security.principal", user);
		map.put("java.naming.security.credentials", password);
		map.put("jmx.remote.protocol.provider.pkgs", "weblogic.management.remote");

		JMXServiceURL jmxService = new JMXServiceURL(protocol, address, Integer.parseInt(port), resource + uri);
		connector = JMXConnectorFactory.connect(jmxService, map);
		connection = connector.getMBeanServerConnection();
	}

	public ObjectName[] getServerRuntimes() throws Exception {
		ObjectName[] runtime = (ObjectName[]) connection.getAttribute(service, "ServerRuntimes");
		List<ObjectName> runtimeObjects = new ArrayList<ObjectName>();

		for (ObjectName currObject : runtime) {
			// String str = (String) connection.getAttribute(runtime[j], "Name");
			runtimeObjects.add(currObject);
		}

		ObjectName[] arrayOfObjectName2 = new ObjectName[runtimeObjects.size()];
		for (int k = 0; k < runtimeObjects.size(); k++) {
			arrayOfObjectName2[k] = (runtimeObjects.get(k));
		}

		return arrayOfObjectName2;
	}

	public void threadPoolRuntime() throws Exception {
		ObjectName[] arrayOfObjectName = getServerRuntimes();

		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		write(hostame + "_" + port + "_ThreadPoolRuntime.dat",
				"datetime;hostame;server;CompletedRequestCount;ExecuteThreadTotalCount;ExecuteThreadIdleCount;HoggingThreadCount;PendingUserRequestCount;QueueLength;StandbyThreadCount;Throughput",
				true);

		int i = arrayOfObjectName.length;
		for (int j = 0; j < i; j++) {
			String str = (String) connection.getAttribute(arrayOfObjectName[j], "Name");
			ObjectName localObjectName = (ObjectName) connection.getAttribute(arrayOfObjectName[j],
					"ThreadPoolRuntime");
			String host = (String) connection.getAttribute(arrayOfObjectName[j], "ListenAddress");

			write(hostame + "_" + port + "_ThreadPoolRuntime.dat",
					dateFormat.format(now) + ";" + host + ";" + str + ";"
							+ connection.getAttribute(localObjectName, "CompletedRequestCount") + ";"
							+ connection.getAttribute(localObjectName, "ExecuteThreadTotalCount") + ";"
							+ connection.getAttribute(localObjectName, "ExecuteThreadIdleCount") + ";"
							+ connection.getAttribute(localObjectName, "HoggingThreadCount") + ";"
							+ connection.getAttribute(localObjectName, "PendingUserRequestCount") + ";"
							+ connection.getAttribute(localObjectName, "QueueLength") + ";"
							+ connection.getAttribute(localObjectName, "StandbyThreadCount") + ";"
							+ connection.getAttribute(localObjectName, "Throughput"));
		}

	}

	public void getJvmRuntime() throws Exception {
		ObjectName[] arrayOfObjectName = getServerRuntimes();
		int i = arrayOfObjectName.length;
		Date localDate = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		write(hostame + "_" + port + "_JVMRuntime.dat",
				"console_id;sistema_id;datetime;hostname;server;HeapFreeCurrent;HeapFreePercent;HeapSizeCurrent;HeapSizeMax",
				true);

		for (int j = 0; j < i; j++) {
			String str = (String) connection.getAttribute(arrayOfObjectName[j], "Name");
			ObjectName localObjectName = (ObjectName) connection.getAttribute(arrayOfObjectName[j], "JVMRuntime");
			String host = (String) connection.getAttribute(arrayOfObjectName[j], "ListenAddress");

			write(hostame + "_" + port + "_JVMRuntime.dat",
					localSimpleDateFormat.format(localDate) + ";" + host + ";" + str + ";"
							+ connection.getAttribute(localObjectName, "HeapFreeCurrent") + ";"
							+ connection.getAttribute(localObjectName, "HeapFreePercent") + ";"
							+ connection.getAttribute(localObjectName, "HeapSizeCurrent") + ";"
							+ connection.getAttribute(localObjectName, "HeapSizeMax"));
		}

	}

	public void getJmsRuntime() throws Exception {
		ObjectName[] arrayOfObjectName1 = getServerRuntimes();
		int i = arrayOfObjectName1.length;

		Date localDate = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		write(hostame + "_" + port + "_JMSServerRuntime.dat",
				"datetime;hostname;server;JMSServerName;DestinationName;MessagesCurrentCount;MessagesPendingCount;MessagesHighCount;MessagesReceivedCount",
				true);

		for (int j = 0; j < i; j++) {
			String str = (String) connection.getAttribute(arrayOfObjectName1[j], "Name");
			String host = (String) connection.getAttribute(arrayOfObjectName1[j], "ListenAddress");
			ObjectName localObjectName = (ObjectName) connection.getAttribute(arrayOfObjectName1[j], "JMSRuntime");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(localObjectName, "JMSServers");

			for (int k = 0; k < arrayOfObjectName2.length; k++) {
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[k],
						"Destinations");
				int m = arrayOfObjectName3.length;

				for (int n = 0; n < m; n++) {
					write(hostame + "_" + port + "_JMSServerRuntime.dat",
							localSimpleDateFormat.format(localDate) + ";" + host + ";" + str + ";"
									+ connection.getAttribute(arrayOfObjectName2[k], "Name") + ";"
									+ connection.getAttribute(arrayOfObjectName3[n], "Name") + ";"
									+ connection.getAttribute(arrayOfObjectName3[n], "MessagesCurrentCount") + ";"
									+ connection.getAttribute(arrayOfObjectName3[n], "MessagesPendingCount") + ";"
									+ connection.getAttribute(arrayOfObjectName3[n], "MessagesHighCount") + ";"
									+ connection.getAttribute(arrayOfObjectName3[n], "MessagesReceivedCount"));
				}
			}
		}

	}

	public void getJdbcRuntime() throws Exception {
		ObjectName[] arrayOfObjectName1 = getServerRuntimes();
		int i = arrayOfObjectName1.length;

		Date localDate = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		write(hostame + "_" + port + "_JDBCDataSourceRuntimeMBeans.dat",
				"datetime;hostname;server;Name;ActiveConnectionsCurrentCount;WaitSecondsHighCount;WaitingForConnectionCurrentCount;WaitingForConnectionFailureTotal;WaitingForConnectionTotal;WaitingForConnectionHighCount",
				true);

		for (int j = 0; j < i; j++) {
			String str = (String) connection.getAttribute(arrayOfObjectName1[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(new ObjectName(
					"com.bea:Name=" + str + ",ServerRuntime=" + str + ",Location=" + str + ",Type=JDBCServiceRuntime"),
					"JDBCDataSourceRuntimeMBeans");
			String host = (String) connection.getAttribute(arrayOfObjectName1[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				write(hostame + "_" + port + "_JDBCDataSourceRuntimeMBeans.dat", localSimpleDateFormat.format(localDate)
						+ ";" + host + ";" + str + ";" + (String) connection.getAttribute(arrayOfObjectName2[m], "Name")
						+ ";" + connection.getAttribute(arrayOfObjectName2[m], "ActiveConnectionsCurrentCount") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitSecondsHighCount") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitingForConnectionCurrentCount") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitingForConnectionFailureTotal") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitingForConnectionTotal") + ";"
						+ connection.getAttribute(arrayOfObjectName2[m], "WaitingForConnectionHighCount"));
			}
		}
	}

	public void getServletData() throws Exception {
		ObjectName[] arrayOfObjectName1 = getServerRuntimes();
		int i = arrayOfObjectName1.length;
		for (int j = 0; j < i; j++) {
			// String str1 = (String) connection.getAttribute(arrayOfObjectName1[j],"Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(arrayOfObjectName1[j],
					"ApplicationRuntimes");
			// String host = (String) connection.getAttribute(arrayOfObjectName1[j],"ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				System.out.println(
						"Application name: " + (String) connection.getAttribute(arrayOfObjectName2[m], "Name"));
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[m],
						"ComponentRuntimes");
				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					System.out.println(
							"  Component name: " + (String) connection.getAttribute(arrayOfObjectName3[i1], "Name"));
					String str2 = (String) connection.getAttribute(arrayOfObjectName3[i1], "Type");
					System.out.println(str2.toString());
					if (str2.toString().equals("WebAppComponentRuntime")) {
						ObjectName[] arrayOfObjectName4 = (ObjectName[]) connection.getAttribute(arrayOfObjectName3[i1],
								"Servlets");
						int i2 = arrayOfObjectName4.length;
						for (int i3 = 0; i3 < i2; i3++) {
							System.out.println("Servlet name: "
									+ (String) connection.getAttribute(arrayOfObjectName4[i3], "Name"));
							System.out.println("Servlet context path: "
									+ (String) connection.getAttribute(arrayOfObjectName4[i3], "ContextPath"));
							System.out.println("Invocation Total Count : "
									+ connection.getAttribute(arrayOfObjectName4[i3], "InvocationTotalCount"));
						}
					}
				}
			}
		}
	}

	public void getEJBData() throws Exception {
		ObjectName[] arrayOfObjectName1 = getServerRuntimes();
		int i = arrayOfObjectName1.length;

		Date localDate = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		write(hostame + "_" + port + "_EJBComponentRuntime.dat",
				"console_id;sistema_id;datetime;hostname;server;AppName;EJBName;AccessTotalCount;MissTotalCount;DestroyedTotalCount;PooledBeansCurrentCount;BeansInUseCurrentCount;WaiterCurrentCount;TimeoutTotalCount",
				true);

		for (int j = 0; j < i; j++) {
			String str1 = (String) connection.getAttribute(arrayOfObjectName1[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(arrayOfObjectName1[j],
					"ApplicationRuntimes");
			String host = (String) connection.getAttribute(arrayOfObjectName1[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[m],
						"ComponentRuntimes");
				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					String str2 = (String) connection.getAttribute(arrayOfObjectName3[i1], "Type");

					if (str2.toString().equals("EJBComponentRuntime")) {
						ObjectName[] arrayOfObjectName4 = (ObjectName[]) connection.getAttribute(arrayOfObjectName3[i1],
								"EJBRuntimes");
						int i2 = arrayOfObjectName4.length;
						for (int i3 = 0; i3 < i2; i3++) {
							ObjectName localObjectName = (ObjectName) connection.getAttribute(arrayOfObjectName4[i3],
									"PoolRuntime");

							write(hostame + "_" + port + "_EJBComponentRuntime.dat",
									localSimpleDateFormat.format(localDate) + ";" + host + ";" + str1 + ";"
											+ (String) connection.getAttribute(arrayOfObjectName2[m], "Name") + ";"
											+ (String) connection.getAttribute(localObjectName, "Name") + ";"
											+ connection.getAttribute(localObjectName, "AccessTotalCount") + ";"
											+ connection.getAttribute(localObjectName, "MissTotalCount") + ";"
											+ connection.getAttribute(localObjectName, "DestroyedTotalCount") + ";"
											+ connection.getAttribute(localObjectName, "PooledBeansCurrentCount") + ";"
											+ connection.getAttribute(localObjectName, "BeansInUseCurrentCount") + ";"
											+ connection.getAttribute(localObjectName, "WaiterCurrentCount") + ";"
											+ connection.getAttribute(localObjectName, "TimeoutTotalCount"));
						}
					}
				}
			}
		}
	}

	public void getWeb() throws Exception {
		ObjectName[] arrayOfObjectName1 = getServerRuntimes();
		int i = arrayOfObjectName1.length;

		Date localDate = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		write(hostame + "_" + port + "_WorkManagerRuntimes.dat",
				"datetime;hostname;server;AppName;WorkManagerName;PendingRequests;CompletedRequests;StuckThreadCount",
				true);

		write(hostame + "_" + port + "_WebAppComponentRuntime.dat",
				"datetime;hostname;server;Name;ComponentName;OpenSessionsCurrentCount;SessionsOpenedTotalCount", true);

		for (int j = 0; j < i; j++) {
			String str1 = (String) connection.getAttribute(arrayOfObjectName1[j], "Name");
			ObjectName[] arrayOfObjectName2 = (ObjectName[]) connection.getAttribute(arrayOfObjectName1[j],
					"ApplicationRuntimes");
			String host = (String) connection.getAttribute(arrayOfObjectName1[j], "ListenAddress");
			int k = arrayOfObjectName2.length;
			for (int m = 0; m < k; m++) {
				ObjectName[] arrayOfObjectName3 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[m],
						"WorkManagerRuntimes");

				int n = arrayOfObjectName3.length;
				for (int i1 = 0; i1 < n; i1++) {
					write(hostame + "_" + port + "_WorkManagerRuntimes.dat", localSimpleDateFormat.format(localDate)
							+ ";" + host + ";" + str1 + ";"
							+ (String) connection.getAttribute(arrayOfObjectName2[m], "Name") + ";"
							+ (String) connection.getAttribute(arrayOfObjectName3[i1], "Name") + ";"
							+ Integer.parseInt(
									connection.getAttribute(arrayOfObjectName3[i1], "PendingRequests").toString())
							+ ";"
							+ Integer.parseInt(
									connection.getAttribute(arrayOfObjectName3[i1], "CompletedRequests").toString())
							+ ";" + Integer.parseInt(
									connection.getAttribute(arrayOfObjectName3[i1], "StuckThreadCount").toString()));
				}

				ObjectName[] arrayOfObjectName4 = (ObjectName[]) connection.getAttribute(arrayOfObjectName2[m],
						"ComponentRuntimes");
				n = arrayOfObjectName4.length;
				for (int i2 = 0; i2 < n; i2++) {
					String str2 = (String) connection.getAttribute(arrayOfObjectName4[i2], "Type");

					if ("WebAppComponentRuntime".equals(str2.toString())) {
						write(hostame + "_" + port + "_WebAppComponentRuntime.dat", localSimpleDateFormat
								.format(localDate) + ";" + host + ";" + str1 + ";"
								+ (String) connection.getAttribute(arrayOfObjectName2[m], "Name") + ";"
								+ (String) connection.getAttribute(arrayOfObjectName4[i2], "ComponentName") + ";"
								+ Integer.parseInt(connection
										.getAttribute(arrayOfObjectName4[i2], "OpenSessionsCurrentCount").toString())
								+ ";" + Integer.parseInt(connection
										.getAttribute(arrayOfObjectName4[i2], "SessionsOpenedTotalCount").toString()));
					}
				}
			}
		}
	}

	public void getCluster() throws Exception {
		ObjectName[] arrayOfObjectName1 = getServerRuntimes();

		Date localDate = new Date();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		write(hostame + "_" + port + "_ClusterRuntime.dat",
				"datetime;hostname;server;Name;ResendRequestsCount;ForeignFragmentsDroppedCount;FragmentsReceivedCount;FragmentsSentCount;MulticastMessagesLostCount",
				true);

		for (ObjectName localObjectName1 : arrayOfObjectName1) {
			String str = (String) connection.getAttribute(localObjectName1, "Name");
			String host = (String) connection.getAttribute(localObjectName1, "ListenAddress");
			ObjectName localObjectName2 = (ObjectName) connection.getAttribute(localObjectName1, "ClusterRuntime");
			if (localObjectName2 != null) {
				write(hostame + "_" + port + "_ClusterRuntime.dat", localSimpleDateFormat.format(localDate) + ";" + host
						+ ";" + str + ";" + (String) connection.getAttribute(localObjectName2, "Name") + ";"
						+ Integer.parseInt(connection.getAttribute(localObjectName2, "ResendRequestsCount").toString())
						+ ";"
						+ Integer.parseInt(
								connection.getAttribute(localObjectName2, "ForeignFragmentsDroppedCount").toString())
						+ ";"
						+ Integer.parseInt(
								connection.getAttribute(localObjectName2, "FragmentsReceivedCount").toString())
						+ ";"
						+ Integer.parseInt(connection.getAttribute(localObjectName2, "FragmentsSentCount").toString())
						+ ";" + connection.getAttribute(localObjectName2, "MulticastMessagesLostCount"));
			}
		}
	}

	public void write(String paramString1, String paramString2, boolean paramBoolean) throws Exception {
		File localFile = null;
		FileWriter localFileWriter = null;
		BufferedWriter localBufferedWriter = null;
		int i = 0;
		try {
			localFile = new File("/" + paramString1);

			if (!localFile.exists()) {
				localFile.createNewFile();
				i = 1;
			}

			if ((!paramBoolean) || ((i != 0) && (paramBoolean))) {
				localFileWriter = new FileWriter(localFile, true);
				localBufferedWriter = new BufferedWriter(localFileWriter);
				localBufferedWriter.write(paramString2);
				localBufferedWriter.newLine();

				localBufferedWriter.close();
				localFileWriter.close();
			}
		} catch (IOException localIOException) {
			localIOException.printStackTrace();
		} catch (Exception localException) {
			localException.printStackTrace();
		} finally {
			if (localBufferedWriter != null) {
				localBufferedWriter.close();
			}

			if (localFileWriter != null)
				localFileWriter.close();
		}
	}

	public void write(String paramString1, String paramString2) throws Exception {
		write(paramString1, paramString2, false);
	}

	public void printNameAndState() throws Exception {
		ObjectName[] arrayOfObjectName = getServerRuntimes();
		System.out.println(" Server   State");
		System.out.println("            .................\n");
		int i = arrayOfObjectName.length;
		for (int j = 0; j < i; j++) {
			String str1 = (String) connection.getAttribute(arrayOfObjectName[j], "Name");
			String str2 = (String) connection.getAttribute(arrayOfObjectName[j], "State");
			System.out.println(str1 + " : " + str2);
		}
	}

	public boolean close() {
		try {
			connector.close();
			return true;

		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.out.println("Usage: java WebLogicMon.jar adm-host adm-port adm-username adm-password");
			System.exit(0);
		}

		String address = args[0]; // addres
		String port = args[1]; // port
		String user = args[2]; // user
		String password = args[3]; // passwd

		WebLogicMon webLogicMon = new WebLogicMon(address, port, user, password);

		try {
			webLogicMon.setService(new ObjectName(
					"com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean"));
		} catch (MalformedObjectNameException localMalformedObjectNameException) {
			throw new AssertionError(localMalformedObjectNameException.getMessage());
		}

		webLogicMon.threadPoolRuntime();
		webLogicMon.getJvmRuntime();
		webLogicMon.getJdbcRuntime();
		webLogicMon.getJmsRuntime();
		webLogicMon.getEJBData();
		webLogicMon.getWeb();
		webLogicMon.getCluster();
		webLogicMon.close();

	}

}
