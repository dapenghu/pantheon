/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.trie;

/**
 * This exception is thrown when there is an issue retrieving or decoding values from {@link
 * MerkleStorage}.
 */
public class MerkleStorageException extends RuntimeException {

  public MerkleStorageException(final String message) {
    super(message);
  }

  public MerkleStorageException(final String message, final Exception cause) {
    super(message, cause);
  }
}
