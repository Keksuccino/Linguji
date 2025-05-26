import de.keksuccino.linguji.linguji.backend.Backend;

public class TestOpenRouterTemperature {
    
    public static void main(String[] args) {
        // Initialize backend
        Backend.init();
        
        // Check temperature value
        System.out.println("Temperature value from config: " + Backend.getOptions().openRouterTemperature.getValue());
        System.out.println("Max tokens value from config: " + Backend.getOptions().openRouterMaxTokens.getValue());
        System.out.println("Top P value from config: " + Backend.getOptions().openRouterTopP.getValue());
        
        // Set a new temperature value
        Backend.getOptions().openRouterTemperature.setValue(0.7);
        System.out.println("After setting to 0.7: " + Backend.getOptions().openRouterTemperature.getValue());
        
        // Force sync
        Backend.getOptions().config.syncConfig();
        
        // Re-read
        Backend.updateOptions();
        System.out.println("After reload: " + Backend.getOptions().openRouterTemperature.getValue());
    }
}
