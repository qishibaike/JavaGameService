package test;

public class RegisterTest {

	public void register() {
		TestClient client = new TestClient(new RegisterTestHandler());
		new Thread(client).start();
	}
}
