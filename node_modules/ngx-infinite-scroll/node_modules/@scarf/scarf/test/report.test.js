const report = require('../report')
const fs = require('fs')

const rateLimitKey = 'testKey'
const tmpFileReturnVal = './scarf-js-test-history.log'
const scarfExecPath = '/path/scarfed-lib-consumer-consumer/node_modules/@scarf/scarf'

function wipeLogHistoryIfPresent () {
  try {
    fs.unlinkSync(tmpFileReturnVal)
  } catch (e) {}
}

describe('Reporting tests', () => {
  beforeAll(() => {
    report.tmpFileName = jest.fn(() => {
      return tmpFileReturnVal
    })
    report.dirName = jest.fn(() => {
      return scarfExecPath
    })
    wipeLogHistoryIfPresent()
  })

  afterAll(() => {
    wipeLogHistoryIfPresent()
  })

  test('Logging rate limit check', () => {
    let history = report.getRateLimitedLogHistory()
    expect(report.hasHitRateLimit(rateLimitKey, history)).toBe(false)
    report.rateLimitedUserLog(rateLimitKey, 'A user message')
    history = report.getRateLimitedLogHistory()
    expect(report.hasHitRateLimit(rateLimitKey, history)).toBe(true)
  })

  test('Redact sensitive data', () => {
    const rootPackageName = '@org/scarfed-lib-consumer-consumer'
    const rootPackageVersion = '1.0.0'
    const grandparentName = 'scarfed-lib-consumer'
    const grandparentVersion = '1.0.1'

    const depInfo = {
      scarf: { name: '@scarf/scarf', version: '0.0.1', path: '/local/directory/deeper/deeper' },
      parent: { name: 'scarfed-library', version: '1.0.0', scarfSettings: { defaultOptIn: true }, path: '/local/directory/deeper/' },
      grandparent: { name: grandparentName, version: grandparentVersion, path: '/local/directory/' },
      rootPackage: { name: rootPackageName, version: rootPackageVersion, packageJsonPath: '/local/directory', path: '/local/directory' }
    }

    const redacted = report.redactSensitivePackageInfo(depInfo)

    expect(redacted.scarf.path).toBeUndefined()
    expect(redacted.parent.path).toBeUndefined()
    expect(redacted.grandparent.path).toBeUndefined()
    expect(redacted.rootPackage.path).toBeUndefined()
    expect(redacted.rootPackage.packageJsonPath).toBeUndefined()

    expect(redacted.grandparent.nameHash).toBe(report.hashWithDefault(grandparentName, 'Fail: used hash fallback for name'))
    expect(redacted.grandparent.versionHash).toBe(report.hashWithDefault(grandparentVersion, 'Fail: used hash fallback for version'))
    expect(redacted.rootPackage.nameHash).toBe(report.hashWithDefault(rootPackageName, 'Fail: used hash fallback for name'))
    expect(redacted.rootPackage.versionHash).toBe(report.hashWithDefault(rootPackageVersion, 'Fail: used hash fallback for version'))

    expect(redacted.grandparent.name).toBeUndefined()
    expect(redacted.grandparent.version).toBeUndefined()
    expect(redacted.rootPackage.name).toBeUndefined()
    expect(redacted.rootPackage.version).toBeUndefined()

    expect(redacted.scarf.name).toBe('@scarf/scarf')
    expect(redacted.scarf.version).toBe('0.0.1')

    expect(redacted.parent.name).toBe('scarfed-library')
    expect(redacted.parent.version).toBe('1.0.0')
  })

  test('Intermediate packages can disable Scarf for their dependents', async () => {
    const exampleLsOutput = fs.readFileSync('./test/example-ls-output.json')

    await expect(new Promise((resolve, reject) => {
      return report.processDependencyTreeOutput(resolve, reject)(null, exampleLsOutput, null)
    })).rejects.toEqual(new Error('Scarf has been disabled via a package.json in the dependency chain.'))

    const parsedLsOutput = JSON.parse(exampleLsOutput)
    delete (parsedLsOutput.dependencies['scarfed-lib-consumer'].scarfSettings)

    await new Promise((resolve, reject) => {
      return report.processDependencyTreeOutput(resolve, reject)(null, JSON.stringify(parsedLsOutput), null)
    }).then(output => {
      expect(output).toBeTruthy()
      expect(output.anyInChainDisabled).toBe(false)
    })
  })

  test('Disable when package manager is yarn', async () => {
    const parsedLsOutput = dependencyTreeScarfEnabled()

    await new Promise((resolve, reject) => {
      return report.processDependencyTreeOutput(resolve, reject)(null, JSON.stringify(parsedLsOutput), null)
    }).then(output => {
      expect(output).toBeTruthy()
      expect(output.anyInChainDisabled).toBe(false)
    })

    // Simulate a yarn install by mocking the env variable npm_execpath
    // leading to a yarn executable
    report.npmExecPath = jest.fn(() => {
      return '/usr/local/lib/node_modules/yarn/bin/yarn.js'
    })

    report.getDependencyInfo = jest.fn(() => {
      return Promise.resolve({
        scarf: { name: '@scarf/scarf', version: '0.0.1' },
        parent: { name: 'scarfed-library', version: '1.0.0', scarfSettings: { defaultOptIn: true } },
        grandparent: { name: 'scarfed-lib-consumer', version: '1.0.0' }
      })
    })

    try {
      await report.reportPostInstall()
      throw new Error("report.reportPostInstall() didn't throw an error")
    } catch (err) {
      expect(err.message).toContain('yarn')
    }
  })

  test('Configurable for top level installs, and enabled for global installs', async () => {
    const parsedLsOutput = dependencyTreeTopLevelInstall()

    await expect(new Promise((resolve, reject) => {
      return report.processDependencyTreeOutput(resolve, reject)(null, JSON.stringify(parsedLsOutput), null)
    })).rejects.toEqual(new Error('The package depending on Scarf is the root package being installed, but Scarf is not configured to run in this case. To enable it, set `scarfSettings.allowTopLevel = true` in your package.json'))

    process.env.npm_config_global = true

    await expect(new Promise((resolve, reject) => {
      return report.processDependencyTreeOutput(resolve, reject)(null, JSON.stringify(parsedLsOutput), null)
    })).toBeTruthy()

    process.env.npm_config_global = ''
    parsedLsOutput.scarfSettings = { allowTopLevel: true }

    await expect(new Promise((resolve, reject) => {
      return report.processDependencyTreeOutput(resolve, reject)(null, JSON.stringify(parsedLsOutput), null)
    })).toBeTruthy()

    parsedLsOutput.scarfSettings = { allowTopLevel: false }
    await expect(new Promise((resolve, reject) => {
      return report.processDependencyTreeOutput(resolve, reject)(null, JSON.stringify(parsedLsOutput), null)
    })).rejects.toEqual(new Error('The package depending on Scarf is the root package being installed, but Scarf is not configured to run in this case. To enable it, set `scarfSettings.allowTopLevel = true` in your package.json'))
  })
})

function dependencyTreeScarfEnabled () {
  const exampleLsOutput = fs.readFileSync('./test/example-ls-output.json')

  const parsedLsOutput = JSON.parse(exampleLsOutput)
  delete (parsedLsOutput.dependencies['scarfed-lib-consumer'].scarfSettings)

  return parsedLsOutput
}

function dependencyTreeTopLevelInstall () {
  const exampleLsOutput = fs.readFileSync('./test/top-level-package-ls-output.json')
  const parsedLsOutput = JSON.parse(exampleLsOutput)
  return parsedLsOutput
}
