package test;

public class QueryOwnResTest {

	public void queryOwnRes() {
		TestClient client = new TestClient(new QueryOwnResTestHandler());
		new Thread(client).start();
	}
}
