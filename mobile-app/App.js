import { StatusBar } from 'expo-status-bar';
import { StyleSheet, View, Text } from 'react-native';
import { WebView } from 'react-native-webview';

// REPLACE THIS WITH YOUR LAPTOP'S IP ADDRESS
// Find it by running 'ipconfig' in terminal
const SERVER_URL = 'http://10.200.49.222:5000';

export default function App() {
  return (
    <View style={styles.container}>
      <StatusBar style="auto" hidden={true} />
      <WebView
        source={{ uri: SERVER_URL }}
        style={{ flex: 1 }}
        javaScriptEnabled={true}
        domStorageEnabled={true}
        allowsInlineMediaPlayback={true}
        mediaPlaybackRequiresUserAction={false}
        onError={(syntheticEvent) => {
          const { nativeEvent } = syntheticEvent;
          console.warn('WebView error: ', nativeEvent);
        }}
        renderError={(errorName) => (
          <View style={styles.errorContainer}>
            <Text style={styles.errorText}>Connection Failed</Text>
            <Text style={styles.errorSubText}>Make sure your laptop and phone are on the same Wi-Fi.</Text>
            <Text style={styles.errorSubText}>Server URL: {SERVER_URL}</Text>
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0f172a',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#0f172a',
    padding: 20,
  },
  errorText: {
    color: '#ef4444',
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  errorSubText: {
    color: '#94a3b8',
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 5,
  }
});
