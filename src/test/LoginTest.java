package test;

public class LoginTest {

	public void login() {
		TestClient client = new TestClient(new LoginTestHandler());
		new Thread(client).start();
	}
}
