import { api, ApiError } from './api';

// Types for Java functions from backend
export interface JavaFunction {
  functionId: string;
  name: string;
  description: string;
  category: string;
  language: 'JAVA' | 'JAVASCRIPT' | 'GROOVY' | 'PYTHON';
  functionSignature: string;
  parameters: string; // JSON string
  functionBody: string;
  builtIn: boolean;
  performanceClass: 'FAST' | 'NORMAL' | 'SLOW';
}

export interface BuiltInFunction {
  name: string;
  category: string;
  description: string;
  functionSignature: string;
  parameters: string;
}

export interface DevelopmentFunctionsResponse {
  developmentMode: boolean;
  builtInFunctions: BuiltInFunction[];
  customFunctions: {
    content: JavaFunction[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
  };
}

export interface FunctionParameter {
  name: string;
  type: 'string' | 'number' | 'boolean' | 'array' | 'object';
  required: boolean;
  description?: string;
}

// Service class for development function operations
export class DevelopmentFunctionService {
  /**
   * Get all available functions (built-in and custom)
   */
  static async getAllFunctions(
    page: number = 0,
    size: number = 20,
    sortBy: string = 'name',
    sortDirection: 'ASC' | 'DESC' = 'ASC'
  ): Promise<DevelopmentFunctionsResponse> {
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        sortBy,
        sortDirection
      });
      
      const response = await api.get<DevelopmentFunctionsResponse>(
        `/development/functions?${params}`
      );
      
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to fetch functions');
    } catch (error) {
      console.error('Error fetching development functions:', error);
      throw error;
    }
  }

  /**
   * Get a specific function by ID
   */
  static async getFunction(functionId: string): Promise<JavaFunction> {
    try {
      const response = await api.get<JavaFunction>(`/development/functions/${functionId}`);
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to fetch function');
    } catch (error) {
      console.error('Error fetching function:', error);
      throw error;
    }
  }

  /**
   * Get a built-in function by name
   */
  static async getBuiltInFunction(functionName: string): Promise<JavaFunction> {
    try {
      const response = await api.get<JavaFunction>(
        `/development/functions/built-in/${functionName}`
      );
      if (response.success && response.data) {
        return response.data;
      }
      throw new ApiError(response.error || 'Failed to fetch built-in function');
    } catch (error) {
      console.error('Error fetching built-in function:', error);
      throw error;
    }
  }

  /**
   * Parse function parameters from JSON string
   */
  static parseParameters(parametersJson: string): FunctionParameter[] {
    try {
      if (!parametersJson) return [];
      return JSON.parse(parametersJson);
    } catch (error) {
      console.error('Error parsing function parameters:', error);
      return [];
    }
  }

  /**
   * Generate Java function call from function name and parameters
   */
  static generateJavaFunctionCall(
    functionName: string,
    parameterValues: Record<string, any>
  ): string {
    // Generate a proper Java function call
    const params = Object.values(parameterValues)
      .map(value => {
        if (typeof value === 'string') {
          return `"${value.replace(/"/g, '\\"')}"`;
        }
        return String(value);
      })
      .join(', ');
    
    return `${functionName}(${params})`;
  }

  /**
   * Convert built-in functions to a format compatible with the field mapping UI
   */
  static convertToUIFormat(functions: BuiltInFunction[]): any[] {
    return functions.map(func => {
      const parameters = this.parseParameters(func.parameters);
      return {
        name: func.name,
        category: func.category || 'general',
        description: func.description,
        parameters: parameters.map(p => ({
          name: p.name,
          type: p.type,
          required: p.required,
          description: p.description
        })),
        javaTemplate: func.functionSignature
      };
    });
  }

  /**
   * Get functions grouped by category
   */
  static async getFunctionsByCategory(): Promise<Record<string, any[]>> {
    const response = await this.getAllFunctions(0, 1000); // Get all functions
    const uiFunctions = this.convertToUIFormat(response.builtInFunctions);
    
    // Group by category
    return uiFunctions.reduce((acc, func) => {
      const category = func.category || 'general';
      if (!acc[category]) {
        acc[category] = [];
      }
      acc[category].push(func);
      return acc;
    }, {} as Record<string, any[]>);
  }
}

export default DevelopmentFunctionService;