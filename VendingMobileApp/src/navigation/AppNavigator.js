import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import {TouchableOpacity, Text} from 'react-native';
import {useAuth} from '../contexts/AuthContext';

import LoginScreen from '../screens/LoginScreen';
import ProductsScreen from '../screens/ProductsScreen';
import MachinesScreen from '../screens/MachinesScreen';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const MainTabs = () => {
  const {logout} = useAuth();

  return (
    <Tab.Navigator
      screenOptions={{
        tabBarActiveTintColor: '#007AFF',
        tabBarInactiveTintColor: '#999',
        headerStyle: {
          backgroundColor: '#007AFF',
        },
        headerTintColor: '#fff',
        headerTitleStyle: {
          fontWeight: 'bold',
        },
      }}>
      <Tab.Screen
        name="Products"
        component={ProductsScreen}
        options={({navigation}) => ({
          headerRight: () => (
            <TouchableOpacity
              onPress={logout}
              style={{marginRight: 15, padding: 5}}>
              <Text style={{color: '#fff', fontSize: 16}}>Logout</Text>
            </TouchableOpacity>
          ),
          tabBarLabel: 'Products',
        })}
      />
      <Tab.Screen
        name="Machines"
        component={MachinesScreen}
        options={({navigation}) => ({
          title: 'Vending Machines',
          headerRight: () => (
            <TouchableOpacity
              onPress={logout}
              style={{marginRight: 15, padding: 5}}>
              <Text style={{color: '#fff', fontSize: 16}}>Logout</Text>
            </TouchableOpacity>
          ),
          tabBarLabel: 'Machines',
        })}
      />
    </Tab.Navigator>
  );
};

const AppNavigator = () => {
  const {isAuthenticated, loading} = useAuth();

  if (loading) {
    return null; // Or a loading screen
  }

  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      {isAuthenticated ? (
        <Stack.Screen name="Main" component={MainTabs} />
      ) : (
        <Stack.Screen name="Login" component={LoginScreen} />
      )}
    </Stack.Navigator>
  );
};

export default AppNavigator;
