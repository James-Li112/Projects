Past projects done in Python.

amazon_review_text_classification.ipynb: An NLP pipeline that predicts product star ratings from review text using six trained classifiers.

Combines review text and summaries, then converts them to features with TF-IDF vectorization and tunes hyperparameters via GridSearchCV with 5 fold cross validation (macro-F1). Trains and compares Logistic Regression, LinearSVC, and Complement Naive Bayes across binary cutoffs and multiclass rating prediction, plus KMeans clustering to group product categories. Evaluated on F1, accuracy, AUC/ROC, precision, recall, and confusion matrices.
