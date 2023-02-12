import unittest

import mita


class TestLibraryInfo(unittest.TestCase):

    def test_version(self):
        with open("version.txt", "r") as f:
            version = f.read()

        self.assertEqual(mita.__version__, version)


if __name__ == '__main__':
    unittest.main()
