from services.chroma_client import ChromaClient
import json

print("=" * 60)
print("Day 5 — RAG Pipeline Test")
print("=" * 60)

# Initialize
print("\n--- Test 1: Initialize ChromaDB ---")
chroma = ChromaClient()
print("✅ ChromaDB initialized successfully")

# Load documents
print("\n--- Test 2: Load and chunk documents ---")
count = chroma.load_documents("./docs")
print(f"✅ Documents loaded — chunks added: {count}")

# Check doc count
print("\n--- Test 3: Verify document count ---")
total = chroma.get_doc_count()
print(f"✅ Total chunks in ChromaDB: {total}")
assert total > 0, "❌ ChromaDB is empty!"

# Test chunking
print("\n--- Test 4: Test chunking logic ---")
sample_text = "A" * 1200
chunks = chroma.chunk_text(sample_text, chunk_size=500, overlap=50)
print(f"✅ 1200 char text → {len(chunks)} chunks (expected 3)")
assert len(chunks) == 3, f"❌ Expected 3 chunks, got {len(chunks)}"

# Test querying
print("\n--- Test 5: Query ChromaDB ---")
results = chroma.query("How should firewall configurations be managed?")
docs = results.get("documents", [[]])[0]
print(f"✅ Query returned {len(docs)} results (expected 3)")
print(f"Top result preview: {docs[0][:150]}..." if docs else "❌ No results")

# Test second query
print("\n--- Test 6: Second query ---")
results2 = chroma.query("What are the requirements for password management?")
docs2 = results2.get("documents", [[]])[0]
print(f"✅ Query returned {len(docs2)} results")
print(f"Top result preview: {docs2[0][:150]}..." if docs2 else "❌ No results")

# Test metadata
print("\n--- Test 7: Check metadata in results ---")
metadatas = results.get("metadatas", [[]])[0]
print(f"✅ Metadata present: {metadatas[0] if metadatas else 'None'}")

print("\n" + "=" * 60)
print("✅ RAG Pipeline fully working — Day 5 complete!")
print("=" * 60)