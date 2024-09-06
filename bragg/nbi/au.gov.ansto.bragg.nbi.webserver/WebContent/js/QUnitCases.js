function add(a, b) {
	return a + b;
}

QUnit.module('add', function() {
	QUnit.test('two numbers', function(assert) {
		const a = 2;
		assert.equal(add(1, a), 3);
	});
});

QUnit.module('SEDB', function() {
	QUnit.test('DBModel', function(assert) {
		assert.equal(add(1, 2), 3);
		const dbModel = new DBModel();
		dbModel.load();
		assert.ok(dbModel != null);
	});
});