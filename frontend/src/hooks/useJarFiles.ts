import { useQuery } from '@tanstack/react-query';
import { jarFileService } from '@/services/jarFileService';
import { JarFile } from '@/types/admin';

interface UseJarFilesOptions {
  driverType?: 'JDBC' | 'IBMMQ' | 'JMS' | 'SAP_JCO' | 'SAP_IDOC';
  vendor?: string;
  enabled?: boolean;
}

// Common JDBC driver mappings
const JDBC_DRIVER_INFO: Record<string, { 
  driverClass: string; 
  urlFormat: string; 
  defaultPort: number;
  vendor: string;
}> = {
  'mysql': {
    vendor: 'MySQL',
    driverClass: 'com.mysql.cj.jdbc.Driver',
    urlFormat: 'jdbc:mysql://{host}:{port}/{database}',
    defaultPort: 3306
  },
  'postgresql': {
    vendor: 'PostgreSQL',
    driverClass: 'org.postgresql.Driver',
    urlFormat: 'jdbc:postgresql://{host}:{port}/{database}',
    defaultPort: 5432
  },
  'oracle': {
    vendor: 'Oracle',
    driverClass: 'oracle.jdbc.driver.OracleDriver',
    urlFormat: 'jdbc:oracle:thin:@{host}:{port}:{database}',
    defaultPort: 1521
  },
  'sqlserver': {
    vendor: 'Microsoft SQL Server',
    driverClass: 'com.microsoft.sqlserver.jdbc.SQLServerDriver',
    urlFormat: 'jdbc:sqlserver://{host}:{port};databaseName={database}',
    defaultPort: 1433
  },
  'db2': {
    vendor: 'IBM DB2',
    driverClass: 'com.ibm.db2.jcc.DB2Driver',
    urlFormat: 'jdbc:db2://{host}:{port}/{database}',
    defaultPort: 50000
  },
  'mariadb': {
    vendor: 'MariaDB',
    driverClass: 'org.mariadb.jdbc.Driver',
    urlFormat: 'jdbc:mariadb://{host}:{port}/{database}',
    defaultPort: 3306
  },
  'h2': {
    vendor: 'H2',
    driverClass: 'org.h2.Driver',
    urlFormat: 'jdbc:h2:tcp://{host}:{port}/{database}',
    defaultPort: 9092
  },
  'teradata': {
    vendor: 'Teradata',
    driverClass: 'com.teradata.jdbc.TeraDriver',
    urlFormat: 'jdbc:teradata://{host}/DATABASE={database}',
    defaultPort: 1025
  },
  'redshift': {
    vendor: 'Amazon Redshift',
    driverClass: 'com.amazon.redshift.jdbc42.Driver',
    urlFormat: 'jdbc:redshift://{host}:{port}/{database}',
    defaultPort: 5439
  },
  'snowflake': {
    vendor: 'Snowflake',
    driverClass: 'net.snowflake.client.jdbc.SnowflakeDriver',
    urlFormat: 'jdbc:snowflake://{account}.snowflakecomputing.com/?warehouse={warehouse}&db={database}',
    defaultPort: 443
  }
};

// IBM MQ driver requirements
const IBMMQ_REQUIRED_JARS = [
  'com.ibm.mq.allclient.jar',
  'com.ibm.mq.jmqi.jar',
  'com.ibm.mq.pcf.jar',
  'com.ibm.mq.headers.jar',
  'com.ibm.mq.commonservices.jar',
  'jms.jar',
  'javax.jms-api.jar'
];

// SAP driver requirements
const SAP_JCO_REQUIRED = {
  jar: 'sapjco3.jar',
  nativeLibraries: {
    windows: ['sapjco3.dll'],
    linux: ['libsapjco3.so'],
    macos: ['libsapjco3.dylib']
  }
};

const SAP_IDOC_REQUIRED = {
  jar: 'sapidoc3.jar',
  dependencies: ['sapjco3.jar'] // IDoc requires JCo
};

export function useJarFiles(options: UseJarFilesOptions = {}) {
  const { driverType, vendor, enabled = true } = options;

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['jarFiles', driverType, vendor],
    queryFn: async () => {
      const response = await jarFileService.getAllJarFiles({
        driverType,
        isActive: true
      });
      
      if (!response.success || !response.data) {
        throw new Error(response.error?.message || 'Failed to fetch JAR files');
      }

      let jarFiles = response.data;

      // Filter by vendor if specified
      if (vendor) {
        jarFiles = jarFiles.filter(jar => 
          jar.vendor?.toLowerCase() === vendor.toLowerCase()
        );
      }

      return jarFiles;
    },
    enabled
  });

  // Get unique vendors from JAR files
  const getAvailableVendors = () => {
    if (!data) return [];
    
    const vendors = new Set<string>();
    data.forEach(jar => {
      if (jar.vendor) {
        vendors.add(jar.vendor);
      }
    });
    
    return Array.from(vendors).sort();
  };

  // Get driver info for a vendor
  const getDriverInfo = (vendorKey: string) => {
    return JDBC_DRIVER_INFO[vendorKey.toLowerCase()];
  };

  // Check if all required IBM MQ JARs are present
  const validateIbmMqDependencies = () => {
    if (!data || driverType !== 'IBMMQ') return { valid: false, missing: IBMMQ_REQUIRED_JARS };
    
    const uploadedJars = data.map(jar => jar.file_name?.toLowerCase() || '');
    const missing = IBMMQ_REQUIRED_JARS.filter(required => 
      !uploadedJars.some(uploaded => uploaded.includes(required.toLowerCase()))
    );
    
    return {
      valid: missing.length === 0,
      missing,
      uploaded: uploadedJars.length,
      required: IBMMQ_REQUIRED_JARS.length
    };
  };

  // Get IBM MQ connection factory info
  const getIbmMqConnectionInfo = () => {
    const primaryJar = data?.find(jar => 
      jar.file_name?.toLowerCase().includes('allclient')
    );

    return {
      connectionFactoryClass: 'com.ibm.mq.jms.MQConnectionFactory',
      queueClass: 'com.ibm.mq.jms.MQQueue',
      version: primaryJar?.version || 'Unknown'
    };
  };

  // Validate SAP JCo dependencies
  const validateSapJcoDependencies = () => {
    if (!data) return { valid: false, missing: { jar: SAP_JCO_REQUIRED.jar, nativeLibrary: true } };
    
    const jarFiles = data.map(jar => jar.file_name?.toLowerCase() || '');
    
    // Check for main JCo JAR
    const hasJcoJar = jarFiles.some(file => file.includes('sapjco3.jar'));
    
    // Check for native library based on platform
    const platform = getPlatform();
    const requiredNativeLibs = SAP_JCO_REQUIRED.nativeLibraries[platform] || [];
    const hasNativeLib = requiredNativeLibs.some(lib => 
      jarFiles.some(file => file.includes(lib.toLowerCase()))
    );
    
    return {
      valid: hasJcoJar && hasNativeLib,
      hasJar: hasJcoJar,
      hasNativeLibrary: hasNativeLib,
      platform,
      missingJar: hasJcoJar ? null : SAP_JCO_REQUIRED.jar,
      missingNativeLibrary: hasNativeLib ? null : requiredNativeLibs[0]
    };
  };

  // Validate SAP IDoc dependencies
  const validateSapIdocDependencies = () => {
    if (!data) return { 
      valid: false, 
      missing: [SAP_IDOC_REQUIRED.jar, ...SAP_IDOC_REQUIRED.dependencies] 
    };
    
    const jarFiles = data.map(jar => jar.file_name?.toLowerCase() || '');
    
    // Check for IDoc JAR
    const hasIdocJar = jarFiles.some(file => file.includes('sapidoc3.jar'));
    
    // Check for JCo dependency
    const jcoValidation = validateSapJcoDependencies();
    
    return {
      valid: hasIdocJar && jcoValidation.valid,
      hasIdocJar,
      hasJcoDependency: jcoValidation.valid,
      missing: [
        !hasIdocJar ? SAP_IDOC_REQUIRED.jar : null,
        !jcoValidation.valid ? 'SAP JCo libraries' : null
      ].filter(Boolean)
    };
  };

  // Get SAP connection info
  const getSapConnectionInfo = () => {
    const jcoJar = data?.find(jar => 
      jar.file_name?.toLowerCase().includes('sapjco3.jar')
    );
    const idocJar = data?.find(jar => 
      jar.file_name?.toLowerCase().includes('sapidoc3.jar')
    );

    return {
      jcoVersion: jcoJar?.version || 'Unknown',
      idocVersion: idocJar?.version || 'Unknown',
      hasJco: !!jcoJar,
      hasIdoc: !!idocJar
    };
  };

  // Helper to detect platform
  const getPlatform = (): 'windows' | 'linux' | 'macos' => {
    // This is a simplified detection - in a real app, you might want to 
    // get this from the backend or use a more sophisticated approach
    const userAgent = navigator.userAgent.toLowerCase();
    if (userAgent.includes('win')) return 'windows';
    if (userAgent.includes('mac')) return 'macos';
    return 'linux';
  };

  return {
    jarFiles: data || [],
    isLoading,
    error,
    refetch,
    getAvailableVendors,
    getDriverInfo,
    validateIbmMqDependencies,
    getIbmMqConnectionInfo,
    validateSapJcoDependencies,
    validateSapIdocDependencies,
    getSapConnectionInfo
  };
}